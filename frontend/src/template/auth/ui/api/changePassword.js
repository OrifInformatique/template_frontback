import api from '../../apiClient';

export const changePassword = async (oldPassword, newPassword) => {
    // 204 No Content on success
    await api.put('/users/me/password', {
        oldPassword,
        newPassword,
    });
};
