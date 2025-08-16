document.addEventListener('DOMContentLoaded', () => {
    const toggle = document.querySelector('.nav-toggle');
    const nav = document.getElementById('top-nav');
    if (toggle && nav) {
        toggle.addEventListener('click', () => {
            const isOpen = nav.classList.toggle('open');
            toggle.setAttribute('aria-expanded', isOpen);
        });
    }
});
