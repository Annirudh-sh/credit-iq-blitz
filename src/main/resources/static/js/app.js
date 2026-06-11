(function () {
    'use strict';

    const API = '/api';
    const QUESTION_TIME = 15;
    const LEADERBOARD_POLL_MS = 3000;

    let state = {
        gameType: null,
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
        if (/iPhone/.test(ua)) return 'iPhone';
        if (/iPad/.test(ua)) return 'iPad';
        const android = ua.match(/;\s*([^;)]+)\s+Build\//) || ua.match(/Android[^;]*;\s*([^;)]+)/);
        if (android) {
            const code = android[1].trim();
            if (code.startsWith('SM-') || code.startsWith('GT-')) return 'Galaxy';
            return code;
        }
        if (/Macintosh/.test(ua)) return 'Mac';
        if (/Windows/.test(ua)) return 'Windows PC';
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

    // ── Leaderboard ──

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
            const data = await api('GET', '/leaderboard?gameType=' + (state.gameType || 'CREDIT_IQ'));
            state.leaderboard = data;
        } catch (e) { /* silent */ }
    }

    function startLeaderboardPolling() {
        pollLeaderboard();
        leaderboardInterval = setInterval(() => pollLeaderboard(), LEADERBOARD_POLL_MS);
    }

    function stopLeaderboardPolling() {
        if (leaderboardInterval) {
            clearInterval(leaderboardInterval);
            leaderboardInterval = null;
        }
    }

    // ══════════════════════════════
    // SCREEN 0: Home / Game Selection
    // ══════════════════════════════

    document.querySelectorAll('.game-card[data-game]').forEach(card => {
        card.addEventListener('click', () => {
            const game = card.dataset.game;
            state.gameType = game;
            if (game === 'CREDIT_IQ') {
                show('screen-start');
            } else if (game === 'CRICKET') {
                show('screen-cricket-start');
            }
        });
    });

    // ══════════════════════════════
    // CREDIT IQ BLITZ (Quiz)
    // ══════════════════════════════

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
            if (seconds <= 0) { clearInterval(timerInterval); autoAdvance(); }
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
        const q = state.questions[state.currentQ];
        const correct = q.correctIndex;
        const options = $('q-options').children;
        for (let i = 0; i < options.length; i++) options[i].style.pointerEvents = 'none';

        options[correct].classList.add('correct');
        if (index !== correct) {
            options[index].classList.add('wrong');
        }

        state.answers.push({ questionId: q.id, selectedIndex: index });
        setTimeout(() => advanceQuestion(), 1000);
    }

    function autoAdvance() {
        state.answers.push({ questionId: state.questions[state.currentQ].id, selectedIndex: -1 });
        advanceQuestion();
    }

    async function advanceQuestion() {
        state.currentQ++;
        if (state.currentQ < state.questions.length) {
            showQuestion();
        } else {
            try {
                const data = await api('POST', `/quiz/${state.attemptId}/submit`, { answers: state.answers });
                state.coins = data.coins;
                state.correctCount = data.correctCount;
                state.timeTakenSec = data.timeTakenSec;
                goToPhoneScreen();
            } catch (e) { toast(e.message); }
        }
    }

    // ══════════════════════════════
    // TB CRICKET
    // ══════════════════════════════

    const cricket = {
        totalBalls: 6,
        currentBall: 0,
        totalRuns: 0,
        ballInFlight: false,
        swung: false,
        ballTimeout: null,
        bowlStart: 0,
        bowlDuration: 1200,
        sweetSpotTime: 0
    };

    $('btn-cricket-start').addEventListener('click', async () => {
        $('btn-cricket-start').disabled = true;
        $('btn-cricket-start').innerHTML = '<span class="spinner"></span>';
        try {
            await waitForLocation(3000);
            const data = await api('POST', '/cricket/start');
            state.attemptId = data.attemptId;
            cricket.totalBalls = data.totalBalls || 6;
            cricket.currentBall = 0;
            cricket.totalRuns = 0;
            showCricketGame();
        } catch (e) {
            toast(e.message);
            $('btn-cricket-start').disabled = false;
            $('btn-cricket-start').textContent = 'Start batting';
        }
    });

    function showCricketGame() {
        show('screen-cricket');
        $('stumps').classList.remove('broken');
        $('tap-hint').style.display = '';
        updateCricketHud();
        setTimeout(() => bowlBall(), 1000);
    }

    function updateCricketHud() {
        $('cricket-ball-count').textContent = `${Math.min(cricket.currentBall + 1, cricket.totalBalls)}/${cricket.totalBalls}`;
        $('cricket-score').textContent = cricket.totalRuns;
        $('cricket-coins').textContent = `${cricket.totalRuns * 10} coins`;
    }

    function bowlBall() {
        if (cricket.currentBall >= cricket.totalBalls) {
            finishCricket();
            return;
        }

        cricket.ballInFlight = true;
        cricket.swung = false;
        $('stumps').classList.remove('broken');
        $('cricket-bat').classList.remove('swing');

        const ball = $('cricket-ball');
        const result = $('shot-result');
        result.className = 'shot-result';

        const field = $('cricket-field');
        const fieldH = field.offsetHeight;
        const stumpBottom = 80;
        const batBottom = 100;
        const targetY = fieldH - batBottom - 20;

        // Random start position
        const startOffsets = [-60, -30, 0, 30, 60];
        const startX = (field.offsetWidth / 2) + startOffsets[Math.floor(Math.random() * startOffsets.length)];

        ball.style.transition = 'none';
        ball.style.left = startX + 'px';
        ball.style.top = '-30px';
        ball.style.opacity = '1';
        ball.style.transform = 'scale(0.6)';
        ball.offsetHeight;

        // Bowl duration varies slightly
        cricket.bowlDuration = 1000 + Math.random() * 400;
        cricket.bowlStart = performance.now();
        cricket.sweetSpotTime = cricket.bowlStart + cricket.bowlDuration * 0.78;

        ball.style.transition = `top ${cricket.bowlDuration}ms cubic-bezier(0.2, 0, 0.8, 1), left ${cricket.bowlDuration}ms ease, transform ${cricket.bowlDuration}ms ease`;
        ball.style.top = targetY + 'px';
        ball.style.left = (field.offsetWidth / 2) + 'px';
        ball.style.transform = 'scale(1)';

        cricket.ballTimeout = setTimeout(() => {
            if (!cricket.swung) {
                handleMiss();
            }
        }, cricket.bowlDuration + 80);
    }

    function handleSwing() {
        if (!cricket.ballInFlight || cricket.swung) return;
        cricket.swung = true;
        clearTimeout(cricket.ballTimeout);

        $('tap-hint').style.display = 'none';
        $('cricket-bat').classList.add('swing');

        const now = performance.now();
        const diff = Math.abs(now - cricket.sweetSpotTime);

        let runs, label, cssClass;
        if (diff < 60) {
            runs = 6; label = 'SIX! 🔥'; cssClass = 'six';
        } else if (diff < 140) {
            runs = 4; label = 'FOUR!'; cssClass = 'four';
        } else if (diff < 240) {
            runs = 2; label = '2 Runs';  cssClass = '';
        } else if (diff < 380) {
            runs = 1; label = '1 Run'; cssClass = '';
        } else {
            runs = 0; label = 'OUT!'; cssClass = 'out';
        }

        if (runs > 0) {
            animateBallHit(runs);
            showShotResult(runs, label, cssClass);
        } else {
            handleMiss();
        }
    }

    function animateBallHit(runs) {
        const ball = $('cricket-ball');
        const field = $('cricket-field');
        const w = field.offsetWidth;

        ball.style.transition = 'top 0.6s ease-out, left 0.5s ease, transform 0.6s ease, opacity 0.5s ease';

        if (runs === 6) {
            ball.style.top = '-80px';
            ball.style.left = (w / 2 + (Math.random() > 0.5 ? 40 : -40)) + 'px';
            ball.style.transform = 'scale(0.3)';
            ball.style.opacity = '0';
        } else if (runs === 4) {
            const side = Math.random() > 0.5 ? w + 20 : -20;
            ball.style.top = '40%';
            ball.style.left = side + 'px';
            ball.style.opacity = '0';
        } else if (runs === 2) {
            ball.style.top = '20%';
            ball.style.left = (Math.random() > 0.5 ? w * 0.8 : w * 0.2) + 'px';
            ball.style.opacity = '0';
        } else {
            ball.style.top = '50%';
            ball.style.left = (w * 0.3 + Math.random() * w * 0.4) + 'px';
            ball.style.opacity = '0';
        }
    }

    function handleMiss() {
        cricket.swung = true;
        cricket.ballInFlight = false;

        const ball = $('cricket-ball');
        const field = $('cricket-field');
        const fieldH = field.offsetHeight;

        ball.style.transition = 'top 0.15s linear';
        ball.style.top = (fieldH - 70) + 'px';

        setTimeout(() => {
            $('stumps').classList.add('broken');
            ball.style.opacity = '0';
        }, 150);

        cricket.currentBall++;
        updateCricketHud();

        const result = $('shot-result');
        result.textContent = 'OUT!';
        result.className = 'shot-result out show';

        setTimeout(() => {
            result.className = 'shot-result';
            $('stumps').classList.remove('broken');
            $('cricket-bat').classList.remove('swing');
            bowlBall();
        }, 1800);
    }

    function showShotResult(runs, label, cssClass) {
        cricket.ballInFlight = false;
        cricket.totalRuns += runs;
        cricket.currentBall++;
        updateCricketHud();

        const result = $('shot-result');
        result.textContent = label;
        result.className = 'shot-result show' + (cssClass ? ' ' + cssClass : '');

        setTimeout(() => {
            result.className = 'shot-result';
            $('cricket-bat').classList.remove('swing');
            const ball = $('cricket-ball');
            ball.style.transition = 'none';
            ball.style.opacity = '0';
            bowlBall();
        }, 1500);
    }

    $('cricket-field').addEventListener('click', handleSwing);
    $('cricket-field').addEventListener('touchstart', (e) => {
        e.preventDefault();
        handleSwing();
    }, { passive: false });

    async function finishCricket() {
        try {
            const data = await api('POST', `/cricket/${state.attemptId}/submit`, {
                totalRuns: cricket.totalRuns
            });
            state.coins = data.coins;
            state.timeTakenSec = data.timeTakenSec;
            goToPhoneScreen();
        } catch (e) { toast(e.message); }
    }

    // ══════════════════════════════
    // SHARED POST-GAME FLOW
    // ══════════════════════════════

    function goToPhoneScreen() {
        startLeaderboardPolling();
        showPhoneScreen();
    }

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

    // ── OTP Screen ──

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

    document.querySelectorAll('.otp-box').forEach((box, i, all) => {
        box.addEventListener('input', (e) => {
            const val = e.target.value.replace(/\D/g, '');
            e.target.value = val.slice(-1);
            if (val && i < all.length - 1) all[i + 1].focus();
            updateVerifyButton();
        });
        box.addEventListener('keydown', (e) => {
            if (e.key === 'Backspace' && !e.target.value && i > 0) all[i - 1].focus();
        });
        box.addEventListener('paste', (e) => {
            e.preventDefault();
            const pasted = (e.clipboardData.getData('text') || '').replace(/\D/g, '');
            for (let j = 0; j < all.length && j < pasted.length; j++) all[j].value = pasted[j];
            const focusIdx = Math.min(pasted.length, all.length - 1);
            all[focusIdx].focus();
            updateVerifyButton();
        });
    });

    document.querySelectorAll('.consent-header').forEach(header => {
        const key = header.dataset.consent;
        const expandIcon = header.querySelector('.consent-expand');
        const body = $(`consent-${key}-body`);
        header.addEventListener('click', (e) => {
            if (e.target.type === 'checkbox') { updateVerifyButton(); return; }
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
        } catch (e) { toast(e.message); }
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

    // ── Result Screen ──

    function showResultScreen() {
        show('screen-result');
        const lb = [...state.leaderboard];
        const displayName = state.name || 'You';
        const userEntry = { rank: state.rank, name: displayName, coins: state.coins };

        const insertIdx = lb.findIndex(e => e.rank >= state.rank);
        if (insertIdx >= 0) {
            lb.splice(insertIdx, 0, userEntry);
            lb.forEach((e, i) => e.rank = i + 1);
        } else {
            userEntry.rank = lb.length + 1;
            lb.push(userEntry);
        }

        const display = lb.slice(0, Math.max(5, state.rank));
        state.leaderboard = display;
        renderLeaderboard('lb-result', state.rank);

        $('result-coins').textContent = state.coins;
        const total = state.totalPlayers.toLocaleString('en-IN');
        $('result-rank-text').textContent = `Rank ${state.rank} of ${total} today`;
    }

    // Leaderboard auto-refresh on active screens
    const _origPoll = pollLeaderboard;
    pollLeaderboard = async function () {
        await _origPoll();
        if ($('screen-phone').classList.contains('active')) renderLeaderboard('lb-phone');
        if ($('screen-otp').classList.contains('active')) renderLeaderboard('lb-otp');
    };

})();
