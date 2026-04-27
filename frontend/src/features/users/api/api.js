import auth from "./authClient";
import api from "../../auth/ui/api/apiClient";

export const getUsers = async () => {
    try {
        const response = await api.get(`/users/all`);
        return response.data;
    }
    catch(error) {
        console.error(`Error while fetching users: ${error.message}`);
        return [];
    }
};
export const deleteUserLocal = async (id) => {
    console.log("userID :" + id);
    try {
        const response = await api.delete(`/users/${id}/false`);
        return response.data;
    }
    catch(error) {
        console.error(`Error while deleting user: ${error.message}`);
        return null;
    }
};

export const getRoles = async () => {
    try {
        const response = await api.get(`/roles/all`);
        return response.data;
    }
    catch(error) {
        console.error(`Error while fetching roles: ${error.message}`);
        return [];
    };
}

export const deleteUserDistant = async (id) => {
    console.log("userID :" + id);
    try {
        const response = await api.delete(`/users/${id}/true`);
        return response.data;
    }
    catch(error) {
        console.error(`Error while deleting user: ${error.message}`);
        return null;
    }
};

export const hardDeleteUserLocal = async (id) => {
    console.log("userID :" + id);
    try {
        const response = await api.delete(`/users/${id}/false/permanent`);
        return response.data;
    }
    catch(error) {
        console.error(`Error while hard deleting user: ${error.message}`);
        return null;
    }
};

export const hardDeleteUserDistant = async (id) => {
    try {
        console.log("userID :" + id);
        const response = await api.delete(`/users/${id}/true/permanent`);
        return response.data;
    }
    catch(error) {
        console.error(`Error while hard deleting user: ${error.message}`);
        return null;
    }
};

export const createUser = async (data) => {
    try {
        const response = await api.post(`/auth/register`, data);
        return response.data;
    }
    catch(error) {
        console.error(`Error while creating user: ${error.message}`);
        return null;
    }
};

export const updateUser = async (id, data) => {
    try {
        const response = await api.put(`/users/${id}`, data);
        return response.data;
    }
    catch(error) {
        console.error(`Error while updating user: ${error.message}`);
        return null;
    }
};

export const updateAuthUser = async (id, data) => {
    try {
        const response = await auth.put(`/users/${id}`, data);
        return response.data;
    }
    catch(error) {
        console.error(`Error while updating authenticated user: ${error.message}`);
        return null;
    }
};

export const createAuthUser = async (data) => {
    try {
        const response = await auth.post(`/auth/register`, data);
        return response.data;
    }
    catch(error) {
        console.error(`Error while creating authenticated user: ${error.message}`);
        return null;
    }
};

export const getUserWithDeleted = async () => {
    try {
        const response = await api.get(`/users/all-with-deleted`);
        return response.data;
    }
    catch(error) {
        console.error(`Error while fetching users with deleted: ${error.message}`);
        return [];
    }
};

export const restoreUser = async (id) => {
    try {
        const response = await api.put(`/users/${id}/restore`);
        return response.data;
    }
    catch(error) {
        console.error(`Error while restoring user: ${error.message}`);
        return null;
    }
};

export const restoreDistantUser = async (id) => {
    try{
        const response = await  auth.put(`/users/${id}/restore`)
        return response.data;
    }
    catch (error) {
        console.error(`Error whil restoring user : ${error.message}`);
        return null
    }
}
