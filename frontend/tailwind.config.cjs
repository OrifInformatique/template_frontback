module.exports = {
    content: ['./src/**/*.{js,jsx,html,ejs}'],
    theme: {
        extend: {
            colors: {
                primary: '#005ba9',
                background: '#f4f7fd',
                disabled: '#e7e7e7',
                danger: '#b91c1c'
            },
            fontFamily: {
                display: ['Montserrat', 'sans-serif'],
            },
        },
    },
    plugins: [
        require('@tailwindcss/forms'),
    ],
};