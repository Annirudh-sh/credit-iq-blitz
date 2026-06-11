(function () {
    'use strict';

    const API = '/api';
    const QUESTION_TIME = 15;
    const LEADERBOARD_POLL_MS = 3000;

    let state = {
        attemptId: null,
        questions: [],
        currentQ: 0,
        answers: [],
        coins: 0,
        correctCount: 0,
        timeTakenSec: 0,
        phone: '',
        name: '',
        rank: 0,
        totalPlayers: 0,
        leaderboard: []
    };

    let timerInterval = null;
    let leaderboardInterval = null;
    let resendInterval = null;

    // ── Device info ──

    function parseDeviceModel(ua) {
        // Android: "... Android 13; SM-G991B ..." or "... Android 12; Pixel 6 ..."
        const android = ua.match(/;\s*([^;)]+)\s+Build\//);
        if (android) return android[1].trim();
        const android2 = ua.match(/Android[^;]*;\s*([^;)]+)/);
        if (android2) return android2[1].trim();
        // iOS: map platform to model
        if (/iPhone/.test(ua)) return 'iPhone';
        if (/iPad/.test(ua)) return 'iPad';
        if (/Macintosh/.test(ua)) return 'Mac';
        if (/Windows/.test(ua)) return 'Windows PC';
        if (/Linux/.test(ua)) return 'Linux';
        return 'Unknown';
    }

    const device = {
        id: localStorage.getItem('deviceId') || (() => {
            const id = crypto.randomUUID();
            localStorage.setItem('deviceId', id);
            return id;
        })(),
        type: /android/i.test(navigator.userAgent) ? 'android'
            : /iphone|ipad|ipod/i.test(navigator.userAgent) ? 'ios'
            : 'web',
        model: parseDeviceModel(navigator.userAgent),
        lat: null,
        lng: null,
        locationReady: false
    };

    // Request location eagerly on page load
    const locationPromise = new Promise(resolve => {
        if (!navigator.geolocation) { resolve(); return; }
        navigator.geolocation.getCurrentPosition(
            pos => {
                device.lat = pos.coords.latitude;
                device.lng = pos.coords.longitude;
                device.locationReady = true;
                resolve();
            },
            () => { resolve(); },
            { enableHighAccuracy: false, timeout: 10000, maximumAge: 300000 }
        );
    });

    // Wait for location with a max timeout (used before first API call)
    function waitForLocation(ms) {
        if (device.locationReady) return Promise.resolve();
        return Promise.race([
            locationPromise,
            new Promise(resolve => setTimeout(resolve, ms))
        ]);
    }

    // ── Helpers ──

    function $(id) { return document.getElementById(id); }

    function show(screenId) {
        document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
        $(screenId).classList.add('active');
    }

    function toast(msg) {
        const el = $('toast');
        el.textContent = msg;
        el.classList.add('show');
        setTimeout(() => el.classList.remove('show'), 3000);
    }

    async function api(method, path, body) {
        const headers = {
            'Content-Type': 'application/json',
            'X-Device-Id': device.id,
            'X-Device-Type': device.type,
            'X-Device-Model': device.model
        };
        if (device.lat != null) headers['X-Latitude'] = String(device.lat);
        if (device.lng != null) headers['X-Longitude'] = String(device.lng);

        const opts = { method, headers };
        if (body) opts.body = JSON.stringify(body);
        const res = await fetch(API + path, opts);
        if (!res.ok) {
            const err = await res.json().catch(() => ({ error: 'Request failed' }));
            throw new Error(err.error || 'Request failed');
        }
        if (res.status === 202) return null;
        return res.json();
    }

    // ── Leaderboard rendering ──

    function renderLeaderboard(containerId, highlightRank) {
        const container = $(containerId);
        container.innerHTML = '';
        state.leaderboard.forEach(entry => {
            const isMe = highlightRank && entry.rank === highlightRank;
            const row = document.createElement('div');
            row.className = 'lb-row' + (isMe ? ' highlight' : '');
            row.innerHTML = `
                <span class="lb-rank">${entry.rank}</span>
                <span class="lb-name">${entry.name}${isMe ? ' (You)' : ''}</span>
                <span class="lb-coins">${entry.coins}</span>
            `;
            container.appendChild(row);
        });
    }

    async function pollLeaderboard() {
        try {
            const data = await api('GET', '/leaderboard');
            state.leaderboard = data;
        } catch (e) { /* silent */ }
    }

    function startLeaderboardPolling() {
        pollLeaderboard();
        leaderboardInterval = setInterval(pollLeaderboard, LEADERBOARD_POLL_MS);
    }

    function stopLeaderboardPolling() {
        if (leaderboardInterval) {
            clearInterval(leaderboardInterval);
            leaderboardInterval = null;
        }
    }

    // ── Screen 1: Start ──

    $('btn-start').addEventListener('click', async () => {
        $('btn-start').disabled = true;
        $('btn-start').innerHTML = '<span class="spinner"></span>';
        try {
            await waitForLocation(3000);
            const data = await api('POST', '/quiz/start');
            state.attemptId = data.attemptId;
            state.questions = data.questions;
            state.currentQ = 0;
            state.answers = [];
            showQuestion();
        } catch (e) {
            toast(e.message);
            $('btn-start').disabled = false;
            $('btn-start').textContent = 'Start the blitz';
        }
    });

    // ── Screen 2: Questions ──

    function showQuestion() {
        show('screen-question');
        const q = state.questions[state.currentQ];
        $('q-count').textContent = `Question ${state.currentQ + 1} of ${state.questions.length}`;
        $('q-text').textContent = q.text;

        const optContainer = $('q-options');
        optContainer.innerHTML = '';
        q.options.forEach((opt, i) => {
            const div = document.createElement('div');
            div.className = 'option';
            div.textContent = opt;
            div.addEventListener('click', () => selectOption(i));
            optContainer.appendChild(div);
        });

        startTimer();
    }

    function startTimer() {
        let seconds = QUESTION_TIME;
        updateTimerDisplay(seconds);
        $('progress-fill').style.width = '100%';

        if (timerInterval) clearInterval(timerInterval);
        timerInterval = setInterval(() => {
            seconds--;
            updateTimerDisplay(seconds);
            $('progress-fill').style.width = ((seconds / QUESTION_TIME) * 100) + '%';

            if (seconds <= 5) {
                $('q-timer').classList.add('urgent');
            }

            if (seconds <= 0) {
                clearInterval(timerInterval);
                autoAdvance();
            }
        }, 1000);
    }

    function updateTimerDisplay(sec) {
        const m = Math.floor(sec / 60);
        const s = sec % 60;
        $('timer-text').textContent = `${m}:${s.toString().padStart(2, '0')}`;
        $('q-timer').classList.toggle('urgent', sec <= 5);
    }

    function selectOption(index) {
        clearInterval(timerInterval);
        const options = $('q-options').children;
        for (let i = 0; i < options.length; i++) {
            options[i].style.pointerEvents = 'none';
        }
        options[index].classList.add('selected');

        state.answers.push({
            questionId: state.questions[state.currentQ].id,
            selectedIndex: index
        });

        setTimeout(() => advanceQuestion(), 600);
    }

    function autoAdvance() {
        state.answers.push({
            questionId: state.questions[state.currentQ].id,
            selectedIndex: -1
        });
        advanceQuestion();
    }

    async function advanceQuestion() {
        state.currentQ++;
        if (state.currentQ < state.questions.length) {
            showQuestion();
        } else {
            await submitQuiz();
        }
    }

    async function submitQuiz() {
        try {
            const data = await api('POST', `/quiz/${state.attemptId}/submit`, {
                answers: state.answers
            });
            state.coins = data.coins;
            state.correctCount = data.correctCount;
            state.timeTakenSec = data.timeTakenSec;
            startLeaderboardPolling();
            showPhoneScreen();
        } catch (e) {
            toast(e.message);
        }
    }

    // ── Screen 3: Phone ──

    function showPhoneScreen() {
        show('screen-phone');
        renderLeaderboard('lb-phone');
        $('name-input').value = '';
        $('phone-input').value = '';
        $('name-input').focus();
    }

    function validatePhoneForm() {
        const name = $('name-input').value.trim();
        const phone = $('phone-input').value.replace(/\D/g, '');
        $('btn-send-otp').disabled = !(name.length > 0 && phone.length === 10);
    }

    $('name-input').addEventListener('input', validatePhoneForm);

    $('phone-input').addEventListener('input', (e) => {
        const val = e.target.value.replace(/\D/g, '');
        e.target.value = val;
        validatePhoneForm();
    });

    $('btn-send-otp').addEventListener('click', async () => {
        const phone = $('phone-input').value.trim();
        const name = $('name-input').value.trim();
        if (phone.length !== 10 || !name) return;

        $('btn-send-otp').disabled = true;
        $('btn-send-otp').innerHTML = '<span class="spinner"></span>';
        try {
            state.phone = phone;
            state.name = name;
            await api('POST', '/otp/request', { phone });
            showOtpScreen();
        } catch (e) {
            toast(e.message);
            $('btn-send-otp').disabled = false;
            $('btn-send-otp').textContent = 'Send OTP';
        }
    });

    // ── Screen 4: OTP + Consent ──

    function showOtpScreen() {
        show('screen-otp');
        renderLeaderboard('lb-otp');

        const masked = state.phone.substring(0, 5) + ' ' + state.phone.substring(5);
        $('otp-sent-text').textContent = `Sent to +91 ${masked}`;

        document.querySelectorAll('.otp-box').forEach(b => { b.value = ''; });
        document.querySelector('.otp-box').focus();

        $('consent-cibil').checked = false;
        $('consent-comms').checked = false;
        updateVerifyButton();
        startResendTimer();
    }

    // OTP box navigation
    document.querySelectorAll('.otp-box').forEach((box, i, all) => {
        box.addEventListener('input', (e) => {
            const val = e.target.value.replace(/\D/g, '');
            e.target.value = val.slice(-1);
            if (val && i < all.length - 1) {
                all[i + 1].focus();
            }
            updateVerifyButton();
        });
        box.addEventListener('keydown', (e) => {
            if (e.key === 'Backspace' && !e.target.value && i > 0) {
                all[i - 1].focus();
            }
        });
        box.addEventListener('paste', (e) => {
            e.preventDefault();
            const pasted = (e.clipboardData.getData('text') || '').replace(/\D/g, '');
            for (let j = 0; j < all.length && j < pasted.length; j++) {
                all[j].value = pasted[j];
            }
            const focusIdx = Math.min(pasted.length, all.length - 1);
            all[focusIdx].focus();
            updateVerifyButton();
        });
    });

    // Consent toggles
    document.querySelectorAll('.consent-header').forEach(header => {
        const key = header.dataset.consent;
        const expandIcon = header.querySelector('.consent-expand');
        const body = $(`consent-${key}-body`);

        header.addEventListener('click', (e) => {
            if (e.target.type === 'checkbox') {
                updateVerifyButton();
                return;
            }
            expandIcon.classList.toggle('open');
            body.classList.toggle('open');
        });
    });

    function getOtpCode() {
        return Array.from(document.querySelectorAll('.otp-box')).map(b => b.value).join('');
    }

    function updateVerifyButton() {
        const code = getOtpCode();
        const cibil = $('consent-cibil').checked;
        const comms = $('consent-comms').checked;
        $('btn-verify').disabled = !(code.length === 4 && cibil && comms);
    }

    $('consent-cibil').addEventListener('change', updateVerifyButton);
    $('consent-comms').addEventListener('change', updateVerifyButton);

    function startResendTimer() {
        let sec = 30;
        $('resend-timer').style.display = '';
        $('btn-resend').style.display = 'none';

        if (resendInterval) clearInterval(resendInterval);
        resendInterval = setInterval(() => {
            sec--;
            $('resend-timer').textContent = `Resend code in 0:${sec.toString().padStart(2, '0')}`;
            if (sec <= 0) {
                clearInterval(resendInterval);
                $('resend-timer').style.display = 'none';
                $('btn-resend').style.display = '';
            }
        }, 1000);
    }

    $('btn-resend').addEventListener('click', async () => {
        try {
            await api('POST', '/otp/request', { phone: state.phone });
            toast('New code sent');
            document.querySelectorAll('.otp-box').forEach(b => { b.value = ''; });
            document.querySelector('.otp-box').focus();
            startResendTimer();
        } catch (e) {
            toast(e.message);
        }
    });

    $('btn-verify').addEventListener('click', async () => {
        $('btn-verify').disabled = true;
        $('btn-verify').innerHTML = '<span class="spinner"></span>';
        try {
            const data = await api('POST', '/otp/verify', {
                phone: state.phone,
                code: getOtpCode(),
                attemptId: state.attemptId,
                name: state.name,
                cibilConsent: $('consent-cibil').checked,
                commsConsent: $('consent-comms').checked
            });
            state.rank = data.rank;
            state.totalPlayers = data.totalPlayers;
            stopLeaderboardPolling();
            await pollLeaderboard();
            showResultScreen();
        } catch (e) {
            toast(e.message);
            $('btn-verify').disabled = false;
            $('btn-verify').textContent = 'Verify & reveal rank';
        }
    });

    // ── Screen 5: Result ──

    function showResultScreen() {
        show('screen-result');

        // Insert user into leaderboard at their rank
        const lb = [...state.leaderboard];
        const displayName = state.name || 'You';
        const userEntry = { rank: state.rank, name: displayName, coins: state.coins };

        // Remove any existing entry at user's rank position if they're within the list
        const insertIdx = lb.findIndex(e => e.rank >= state.rank);
        if (insertIdx >= 0) {
            lb.splice(insertIdx, 0, userEntry);
            // Re-number ranks and cap at display size
            lb.forEach((e, i) => e.rank = i + 1);
        } else {
            userEntry.rank = lb.length + 1;
            lb.push(userEntry);
        }

        // Show top entries + user if outside top
        const display = lb.slice(0, Math.max(5, state.rank));
        state.leaderboard = display;
        renderLeaderboard('lb-result', state.rank);

        $('result-coins').textContent = state.coins;
        const total = state.totalPlayers.toLocaleString('en-IN');
        $('result-rank-text').textContent = `Rank ${state.rank} of ${total} today`;
    }

    // btn-claim is now a direct WhatsApp link — no JS handler needed

    // ── Leaderboard auto-refresh on screens 3 & 4 ──

    const originalPoll = pollLeaderboard;
    pollLeaderboard = async function () {
        await originalPoll();
        if ($('screen-phone').classList.contains('active')) {
            renderLeaderboard('lb-phone');
        }
        if ($('screen-otp').classList.contains('active')) {
            renderLeaderboard('lb-otp');
        }
    };

})();
