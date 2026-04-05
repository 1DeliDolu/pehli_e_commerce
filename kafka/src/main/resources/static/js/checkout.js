(function () {
    const form = document.querySelector('[data-checkout-form]');
    if (!form) {
        return;
    }

    const panels = Array.from(form.querySelectorAll('[data-checkout-step-panel]'));
    const indicators = Array.from(document.querySelectorAll('[data-checkout-step-indicator]'));

    if (panels.length === 0) {
        return;
    }

    let currentStep = Number.parseInt(form.dataset.checkoutStartStep || '1', 10) - 1;
    if (Number.isNaN(currentStep) || currentStep < 0 || currentStep >= panels.length) {
        currentStep = 0;
    }

    function updateStepUi() {
        panels.forEach(function (panel, index) {
            panel.classList.toggle('d-none', index !== currentStep);
        });

        indicators.forEach(function (indicator, index) {
            indicator.classList.toggle('is-active', index === currentStep);
            indicator.classList.toggle('is-complete', index < currentStep);
        });
    }

    function validateCurrentStep() {
        const fields = Array.from(
            panels[currentStep].querySelectorAll('input, select, textarea')
        ).filter(function (field) {
            return !field.disabled && field.type !== 'hidden';
        });

        for (const field of fields) {
            if (!field.checkValidity()) {
                field.reportValidity();
                return false;
            }
        }

        return true;
    }

    form.querySelectorAll('[data-checkout-next]').forEach(function (button) {
        button.addEventListener('click', function () {
            if (!validateCurrentStep()) {
                return;
            }

            if (currentStep < panels.length - 1) {
                currentStep += 1;
                updateStepUi();
            }
        });
    });

    form.querySelectorAll('[data-checkout-back]').forEach(function (button) {
        button.addEventListener('click', function () {
            if (currentStep > 0) {
                currentStep -= 1;
                updateStepUi();
            }
        });
    });

    updateStepUi();
}());
