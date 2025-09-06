// HTTP Server JavaScript
document.addEventListener('DOMContentLoaded', function() {
    initializePage();
    setupCalculator();
});

function initializePage() {
    // Add click effects to link cards
    const linkCards = document.querySelectorAll('.link-card');
    linkCards.forEach(card => {
        card.addEventListener('click', function(e) {
            createRippleEffect(e, this);
        });
    });
    
    // Add status indicator animation
    animateStatusIndicator();
    
    // Add feature list animations
    animateFeatureList();
    
    // Add current time to footer
    updateCurrentTime();
}

function createRippleEffect(event, element) {
    const ripple = document.createElement('span');
    const rect = element.getBoundingClientRect();
    const size = Math.max(rect.width, rect.height);
    const x = event.clientX - rect.left - size / 2;
    const y = event.clientY - rect.top - size / 2;
    
    ripple.style.width = ripple.style.height = size + 'px';
    ripple.style.left = x + 'px';
    ripple.style.top = y + 'px';
    ripple.classList.add('ripple');
    
    element.appendChild(ripple);
    
    setTimeout(() => {
        ripple.remove();
    }, 600);
}

function animateStatusIndicator() {
    const statusIndicator = document.querySelector('.status-indicator');
    if (statusIndicator) {
        statusIndicator.style.animation = 'pulse 2s infinite';
    }
}

function animateFeatureList() {
    const features = document.querySelectorAll('.features li');
    features.forEach((feature, index) => {
        feature.style.opacity = '0';
        feature.style.transform = 'translateX(-20px)';
        
        setTimeout(() => {
            feature.style.transition = 'all 0.5s ease';
            feature.style.opacity = '1';
            feature.style.transform = 'translateX(0)';
        }, index * 100);
    });
}

function updateCurrentTime() {
    const footer = document.querySelector('.footer');
    if (footer) {
        const timeElement = document.getElementById('current-time') || document.createElement('p');
        if (!timeElement.id) {
            timeElement.id = 'current-time';
            footer.appendChild(timeElement);
        }
        
        setInterval(() => {
            const now = new Date();
            timeElement.textContent = `🕐 Server Time: ${now.toLocaleString()}`;
        }, 1000);
    }
}

function setupCalculator() {
    const display = document.getElementById('calc-display');
    const status = document.getElementById('calc-status');
    const history = document.getElementById('calc-history');
    const buttons = document.querySelectorAll('.btn');
    let currentInput = '';
    let calculationHistory = [];

    // Add button click handlers
    buttons.forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            handleButtonClick(btn);
        });

        // Add visual feedback
        btn.addEventListener('mousedown', () => {
            btn.style.transform = 'translateY(0) scale(0.95)';
        });

        btn.addEventListener('mouseup', () => {
            btn.style.transform = '';
        });

        btn.addEventListener('mouseleave', () => {
            btn.style.transform = '';
        });
    });

    function handleButtonClick(btn) {
        if (btn.id === 'clear') {
            currentInput = '';
            display.value = '0';
            status.textContent = '';
            status.className = 'calc-status';
        } else if (btn.id === 'backspace') {
            currentInput = currentInput.slice(0, -1);
            display.value = currentInput || '0';
        } else if (btn.id === 'equals') {
            if (currentInput.trim()) {
                calculateOnServer(currentInput);
            }
        } else {
            const value = btn.dataset.value || '';
            currentInput += value;
            display.value = currentInput;
        }
    }

    // Add keyboard support
    document.addEventListener('keydown', (e) => {
        // Prevent default behavior for calculator keys
        const calculatorKeys = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
                               '+', '-', '*', '/', '(', ')', '.', 'Enter', '=', 
                               'Backspace', 'Escape', 'Delete'];
        
        if (calculatorKeys.includes(e.key)) {
            e.preventDefault();
            
            if (e.key >= '0' && e.key <= '9' || e.key === '.') {
                currentInput += e.key;
                display.value = currentInput;
            } else if (['+', '-', '*', '/', '(', ')'].includes(e.key)) {
                currentInput += e.key;
                display.value = currentInput;
            } else if (e.key === 'Enter' || e.key === '=') {
                if (currentInput.trim()) {
                    calculateOnServer(currentInput);
                }
            } else if (e.key === 'Backspace') {
                currentInput = currentInput.slice(0, -1);
                display.value = currentInput || '0';
            } else if (e.key === 'Escape' || e.key === 'Delete') {
                currentInput = '';
                display.value = '0';
                status.textContent = '';
                status.className = 'calc-status';
            }
        }
    });

    async function calculateOnServer(expression) {
        status.textContent = 'Calculating...';
        status.className = 'calc-status calculating';
        
        try {
            // Simulate server calculation with delay for realistic feel
            await new Promise(resolve => setTimeout(resolve, 500));
            
            const result = evaluateExpression(expression);
            
            if (result !== null && !isNaN(result) && isFinite(result)) {
                display.value = result.toString();
                currentInput = result.toString();
                status.textContent = '✓ Calculated';
                status.className = 'calc-status success';
                
                // Add to history
                addToHistory(expression, result);
            } else {
                display.value = 'Error';
                status.textContent = 'Invalid expression';
                status.className = 'calc-status error';
                currentInput = '';
            }
        } catch (error) {
            display.value = 'Error';
            status.textContent = 'Calculation failed';
            status.className = 'calc-status error';
            currentInput = '';
        }
    }

    // Safe expression evaluator
    function evaluateExpression(expr) {
        try {
            // Replace display operators with actual operators
            let cleanExpr = expr
                .replace(/×/g, '*')
                .replace(/÷/g, '/')
                .replace(/−/g, '-');
            
            // Basic validation - only allow numbers, operators, parentheses, and decimal points
            if (!/^[0-9+\-*/.() ]+$/.test(cleanExpr)) {
                throw new Error('Invalid characters');
            }
            
            // Prevent dangerous expressions
            if (cleanExpr.includes('**') || cleanExpr.includes('++') || cleanExpr.includes('--')) {
                throw new Error('Invalid operator sequence');
            }
            
            // Use Function constructor for safe evaluation (better than eval)
            const result = new Function('return ' + cleanExpr)();
            
            // Round to prevent floating point precision issues
            if (typeof result === 'number' && isFinite(result)) {
                return Math.round(result * 1000000000) / 1000000000;
            }
            
            return result;
        } catch (error) {
            return null;
        }
    }

    function addToHistory(expression, result) {
        const historyItem = document.createElement('div');
        historyItem.className = 'history-item';
        historyItem.innerHTML = `
            <span class="expression">${expression}</span>
            <span class="result">= ${result}</span>
        `;
        
        // Add fade-in animation
        historyItem.style.opacity = '0';
        historyItem.style.transform = 'translateY(-10px)';
        
        history.insertBefore(historyItem, history.firstChild);
        
        // Animate in
        setTimeout(() => {
            historyItem.style.transition = 'all 0.3s ease';
            historyItem.style.opacity = '1';
            historyItem.style.transform = 'translateY(0)';
        }, 10);
        
        // Keep only last 10 calculations
        while (history.children.length > 10) {
            const lastChild = history.lastChild;
            lastChild.style.transition = 'all 0.3s ease';
            lastChild.style.opacity = '0';
            lastChild.style.transform = 'translateY(10px)';
            
            setTimeout(() => {
                if (lastChild.parentNode) {
                    lastChild.parentNode.removeChild(lastChild);
                }
            }, 300);
        }
        
        // Store in memory
        calculationHistory.unshift({ expression, result });
        if (calculationHistory.length > 10) {
            calculationHistory = calculationHistory.slice(0, 10);
        }
    }

    // Initialize display
    display.value = '0';
}

// Add button press effects for all interactive elements
document.addEventListener('DOMContentLoaded', function() {
    const interactiveElements = document.querySelectorAll('button, .link-card, .btn');
    
    interactiveElements.forEach(element => {
        element.addEventListener('mousedown', function() {
            this.style.transform = 'scale(0.95)';
        });
        
        element.addEventListener('mouseup', function() {
            this.style.transform = '';
        });
        
        element.addEventListener('mouseleave', function() {
            this.style.transform = '';
        });
    });
});

// Add smooth scrolling for internal links
document.addEventListener('click', function(e) {
    if (e.target.tagName === 'A' && e.target.getAttribute('href')) {
        const href = e.target.getAttribute('href');
        if (href.startsWith('#')) {
            e.preventDefault();
            const targetElement = document.querySelector(href);
            if (targetElement) {
                targetElement.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        }
    }
});

// Add intersection observer for animations
const observerOptions = {
    threshold: 0.1,
    rootMargin: '0px 0px -50px 0px'
};

const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.style.opacity = '1';
            entry.target.style.transform = 'translateY(0)';
        }
    });
}, observerOptions);

// Observe all content sections for scroll animations
document.addEventListener('DOMContentLoaded', function() {
    const sections = document.querySelectorAll('.content > section');
    sections.forEach(section => {
        section.style.opacity = '0';
        section.style.transform = 'translateY(20px)';
        section.style.transition = 'all 0.6s ease';
        observer.observe(section);
    });
});

// Add typing effect for hero title (optional enhancement)
function typeWriterEffect() {
    const heroTitle = document.querySelector('.hero h1');
    if (heroTitle && !heroTitle.dataset.animated) {
        const originalText = heroTitle.textContent;
        heroTitle.textContent = '';
        heroTitle.dataset.animated = 'true';
        
        let i = 0;
        const typeTimer = setInterval(() => {
            heroTitle.textContent += originalText.charAt(i);
            i++;
            if (i > originalText.length) {
                clearInterval(typeTimer);
            }
        }, 100);
    }
}

// Add error handling for broken links
document.addEventListener('click', function(e) {
    if (e.target.tagName === 'A') {
        const href = e.target.getAttribute('href');
        if (href && href.startsWith('/')) {
            e.preventDefault();
            // Show a notification that this would normally navigate to server route
            showNotification('This would navigate to: ' + href, 'info');
        }
    }
});

// Simple notification system
function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    
    Object.assign(notification.style, {
        position: 'fixed',
        top: '20px',
        right: '20px',
        padding: '12px 20px',
        backgroundColor: type === 'error' ? 'var(--accent-error)' : 
                        type === 'success' ? 'var(--accent-success)' : 'var(--accent-secondary)',
        color: 'white',
        borderRadius: '12px',
        zIndex: '1000',
        opacity: '0',
        transform: 'translateY(-20px)',
        transition: 'all 0.3s ease',
        fontSize: '14px',
        fontWeight: '500',
        boxShadow: 'var(--shadow-glass)'
    });
    
    document.body.appendChild(notification);
    
    // Animate in
    setTimeout(() => {
        notification.style.opacity = '1';
        notification.style.transform = 'translateY(0)';
    }, 100);
    
    // Remove after 3 seconds
    setTimeout(() => {
        notification.style.opacity = '0';
        notification.style.transform = 'translateY(-20px)';
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 300);
    }, 3000);
}