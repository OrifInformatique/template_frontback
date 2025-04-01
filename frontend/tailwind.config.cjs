module.exports = {
    content: ['./src/**/*.{js,jsx,html,ejs}'],
    theme: {
        extend: {
            colors: {
                primary: '#005ba9',
                background: '#f4f7fd',
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