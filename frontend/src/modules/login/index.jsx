import React, { useState } from 'react';
import axios from 'axios';
import Logo from '../../ui/logo';
import Title from '../../ui/title';

const Login = () => {
    const [activeTab, setActiveTab] = useState('login');
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');

    const handleLogin = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.post('http://localhost:8080/auth/login', {
                login: username,
                password,
            });
            console.log('Login successful:', response.data);
            // Handle successful login (store token, redirect, etc.)
        } catch (error) {
            console.error('Login failed:', error.response?.data || error.message);
        }
    };

    const handleRegister = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.post('http://localhost:8080/auth/register', {
                firstName,
                lastName,
                login: username,
                password,
            });
            console.log('Registration successful:', response.data);
            // Optionally switch to login tab after successful registration
            setActiveTab('login');
        } catch (error) {
            console.error('Registration failed:', error.response?.data || error.message);
        }
    };

    const handleOAuth2Login = () => {
        window.location.href = `http://localhost:8080/oauth2/authorization/azure`;
    };

    return (
        <div className="flex flex-col items-center">
            <Logo />
            <Title className="pt-4 sm:pt-6">Section Informatique</Title>

            <div className="mt-8 w-full max-w-md">
                <div className="flex mb-4">
                    <button
                        onClick={() => setActiveTab('login')}
                        className={`flex-1 py-2 px-4 text-center ${activeTab === 'login' ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-700'
                            } rounded-l-md`}
                    >
                        Login
                    </button>
                    <button
                        onClick={() => setActiveTab('register')}
                        className={`flex-1 py-2 px-4 text-center ${activeTab === 'register' ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-700'
                            } rounded-r-md`}
                    >
                        Register
                    </button>
                </div>

                {activeTab === 'login' && (
                    <form onSubmit={handleLogin} className="space-y-4">
                        <div>
                            <label htmlFor="username" className="block mb-2 text-sm font-medium">
                                Username
                            </label>
                            <input
                                type="text"
                                id="username"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                className="w-full px-3 py-2 border rounded-md"
                                required
                            />
                        </div>
                        <div>
                            <label htmlFor="password" className="block mb-2 text-sm font-medium">
                                Password
                            </label>
                            <input
                                type="password"
                                id="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                className="w-full px-3 py-2 border rounded-md"
                                required
                            />
                        </div>
                        <button
                            type="submit"
                            className="w-full px-4 py-2 text-white bg-blue-500 rounded-md hover:bg-blue-600"
                        >
                            Login
                        </button>
                        <button
                            type="button"
                            onClick={() => handleOAuth2Login()}
                            className="w-full px-4 py-2 text-white bg-gray-700 rounded-md hover:bg-gray-800 flex items-center justify-center"
                        >
                            <svg className="w-5 h-5 mr-2" viewBox="0 0 21 21" xmlns="http://www.w3.org/2000/svg">
                                <rect x="1" y="1" width="9" height="9" fill="#f25022" />
                                <rect x="1" y="11" width="9" height="9" fill="#00a4ef" />
                                <rect x="11" y="1" width="9" height="9" fill="#7fba00" />
                                <rect x="11" y="11" width="9" height="9" fill="#ffb900" />
                            </svg>
                            Login with Microsoft
                        </button>
                    </form>
                )}

                {activeTab === 'register' && (
                    <form onSubmit={handleRegister} className="space-y-4">
                        <div>
                            <label htmlFor="firstName" className="block mb-2 text-sm font-medium">
                                First Name
                            </label>
                            <input
                                type="text"
                                id="firstName"
                                value={firstName}
                                onChange={(e) => setFirstName(e.target.value)}
                                className="w-full px-3 py-2 border rounded-md"
                                required
                            />
                        </div>
                        <div>
                            <label htmlFor="lastName" className="block mb-2 text-sm font-medium">
                                Last Name
                            </label>
                            <input
                                type="text"
                                id="lastName"
                                value={lastName}
                                onChange={(e) => setLastName(e.target.value)}
                                className="w-full px-3 py-2 border rounded-md"
                                required
                            />
                        </div>
                        <div>
                            <label htmlFor="regUsername" className="block mb-2 text-sm font-medium">
                                Username
                            </label>
                            <input
                                type="text"
                                id="regUsername"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                className="w-full px-3 py-2 border rounded-md"
                                required
                            />
                        </div>
                        <div>
                            <label htmlFor="regPassword" className="block mb-2 text-sm font-medium">
                                Password
                            </label>
                            <input
                                type="password"
                                id="regPassword"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                className="w-full px-3 py-2 border rounded-md"
                                required
                            />
                        </div>
                        <button
                            type="submit"
                            className="w-full px-4 py-2 text-white bg-blue-500 rounded-md hover:bg-blue-600"
                        >
                            Register
                        </button>
                    </form>
                )}
            </div>
        </div>
    );
};

export default Login;

