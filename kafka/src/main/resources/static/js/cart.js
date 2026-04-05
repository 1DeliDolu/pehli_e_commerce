document.addEventListener('DOMContentLoaded', function () {
    const cartBadges = document.querySelectorAll('[data-cart-count-badge]');
    const cartSummaries = document.querySelectorAll('[data-cart-count-summary]');
    const feedbackHost = createFeedbackHost();

    function createFeedbackHost() {
        const host = document.createElement('div');
        host.className = 'position-fixed top-0 end-0 p-3 app-cart-feedback';
        host.style.zIndex = '1080';
        document.body.appendChild(host);
        return host;
    }

    function showFeedback(message, type) {
        feedbackHost.innerHTML = '';

        const alert = document.createElement('div');
        alert.className = 'alert alert-' + type + ' alert-dismissible fade show shadow-sm mb-0';
        alert.role = 'alert';
        alert.innerHTML = `
            <span>${message}</span>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        `;
        feedbackHost.appendChild(alert);

        window.setTimeout(function () {
            if (alert.parentNode) {
                bootstrap.Alert.getOrCreateInstance(alert).close();
            }
        }, 2500);
    }

    function updateCartIndicators(count) {
        cartBadges.forEach(function (badge) {
            badge.textContent = String(count);
            badge.classList.toggle('d-none', count <= 0);
        });

        cartSummaries.forEach(function (summary) {
            summary.textContent = count > 0 ? `${count} items in your cart` : 'Cart is empty';
            summary.classList.toggle('d-none', count <= 0);
        });
    }

    function formatCurrency(amount) {
        return `${amount.toFixed(2)} EUR`;
    }

    function normalizeQuantity(input) {
        const value = Number.parseInt(input.value, 10);
        const normalizedValue = Number.isNaN(value) || value < 0 ? 0 : value;
        input.value = String(normalizedValue);
        return normalizedValue;
    }

    function updateRowPreview(row) {
        const quantityInput = row.querySelector('[data-cart-quantity-input]');
        const lineTotal = row.querySelector('[data-cart-line-total]');
        const unitPrice = Number.parseFloat(row.dataset.cartUnitPrice || '0');

        if (!quantityInput || !lineTotal) {
            return;
        }

        const quantity = normalizeQuantity(quantityInput);
        lineTotal.textContent = formatCurrency(unitPrice * quantity);
    }

    function recalculateCartSummary() {
        if (!cartPage) {
            return;
        }

        let totalQuantity = 0;
        let subtotalAmount = 0;

        cartPage.querySelectorAll('[data-cart-row]').forEach(function (row) {
            const quantityInput = row.querySelector('[data-cart-quantity-input]');
            const unitPrice = Number.parseFloat(row.dataset.cartUnitPrice || '0');
            if (!quantityInput) {
                return;
            }

            const quantity = normalizeQuantity(quantityInput);
            totalQuantity += quantity;
            subtotalAmount += unitPrice * quantity;
        });

        const subtotal = cartPage.querySelector('[data-cart-subtotal]');
        if (subtotal) {
            subtotal.textContent = formatCurrency(subtotalAmount);
        }

        const totalQuantityElement = cartPage.querySelector('[data-cart-total-quantity]');
        if (totalQuantityElement) {
            totalQuantityElement.textContent = String(totalQuantity);
        }
    }

    async function submitCartForm(form) {
        const submitButtons = form.querySelectorAll('button');
        const body = new URLSearchParams(new FormData(form));
        const endpoint = form.dataset.cartEndpoint || form.action;

        submitButtons.forEach(function (button) {
            button.disabled = true;
        });

        try {
            const response = await fetch(endpoint, {
                method: form.method || 'POST',
                headers: {
                    'Accept': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body
            });

            if (!response.ok) {
                throw new Error('Request failed');
            }

            return await response.json();
        } finally {
            submitButtons.forEach(function (button) {
                button.disabled = false;
            });
        }
    }

    document.querySelectorAll('[data-cart-add-form]').forEach(function (form) {
        form.addEventListener('submit', async function (event) {
            event.preventDefault();

            try {
                const result = await submitCartForm(form);
                updateCartIndicators(result.cartItemCount);
                showFeedback(result.message, 'success');
            } catch (error) {
                showFeedback('Cart operation failed.', 'danger');
            }
        });
    });

    const cartPage = document.querySelector('[data-cart-page]');
    if (!cartPage) {
        return;
    }

    function syncCartPage(result) {
        updateCartIndicators(result.cartItemCount);

        const subtotal = cartPage.querySelector('[data-cart-subtotal]');
        if (subtotal) {
            subtotal.textContent = result.cartSubtotal;
        }

        const totalQuantity = cartPage.querySelector('[data-cart-total-quantity]');
        if (totalQuantity) {
            totalQuantity.textContent = String(result.cartItemCount);
        }

        const row = cartPage.querySelector(`[data-cart-row="${result.productId}"]`);
        if (row && result.productQuantity > 0) {
            const quantityInput = row.querySelector('[data-cart-quantity-input]');
            const lineTotal = row.querySelector('[data-cart-line-total]');

            if (quantityInput) {
                quantityInput.value = String(result.productQuantity);
            }

            if (lineTotal) {
                lineTotal.textContent = result.productLineTotal;
            }
        }

        if (row && result.productQuantity <= 0) {
            row.remove();
        }

        const cartContent = cartPage.querySelector('[data-cart-content]');
        const cartSummary = cartPage.querySelector('[data-cart-summary]');
        const emptyState = cartPage.querySelector('[data-cart-empty-state]');

        if (result.empty) {
            cartContent.classList.add('d-none');
            cartSummary.classList.add('d-none');
            emptyState.classList.remove('d-none');
            return;
        }

        cartContent.classList.remove('d-none');
        cartSummary.classList.remove('d-none');
        emptyState.classList.add('d-none');
    }

    document.querySelectorAll('[data-cart-update-form]').forEach(function (form) {
        const quantityInput = form.querySelector('[data-cart-quantity-input]');

        async function handleUpdate(event) {
            if (event) {
                event.preventDefault();
            }

            window.clearTimeout(form._cartUpdateTimer);

            try {
                const result = await submitCartForm(form);
                syncCartPage(result);
                recalculateCartSummary();
                showFeedback(result.message, 'success');
            } catch (error) {
                showFeedback('Cart update failed.', 'danger');
            }
        }

        form.addEventListener('submit', handleUpdate);

        if (quantityInput) {
            quantityInput.addEventListener('input', function () {
                const row = form.closest('[data-cart-row]');
                if (row) {
                    updateRowPreview(row);
                    recalculateCartSummary();
                }

                window.clearTimeout(form._cartUpdateTimer);
                form._cartUpdateTimer = window.setTimeout(function () {
                    handleUpdate();
                }, 500);
            });

            quantityInput.addEventListener('change', function (event) {
                handleUpdate(event);
            });

            quantityInput.addEventListener('keydown', function (event) {
                if (event.key === '-' || event.key === '+') {
                    event.preventDefault();
                }
            });
        }
    });

    document.querySelectorAll('[data-cart-remove-form]').forEach(function (form) {
        form.addEventListener('submit', async function (event) {
            event.preventDefault();

            try {
                const result = await submitCartForm(form);
                syncCartPage(result);
                showFeedback(result.message, 'success');
            } catch (error) {
                showFeedback('Failed to remove item from cart.', 'danger');
            }
        });
    });

    cartPage.querySelectorAll('[data-cart-row]').forEach(function (row) {
        updateRowPreview(row);
    });
    recalculateCartSummary();
});
