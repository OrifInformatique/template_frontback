import axios from 'axios';

export const handleLoginFormSubmit = (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const identifier = formData.get('identifier');
    const password = formData.get('password');
    try {
        const response = axios.post('/auth/login', {
            login: identifier,
            password: password,
        });
        return response.data.token;
    } catch (error) {
        console.error('Erreur lors de la connexion :', error);
        throw error;
    }
};
export default handleLoginFormSubmit;
