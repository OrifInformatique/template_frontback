import React, { useState, useEffect } from 'react';
import axios from 'axios';
import Logo from '../../ui/logo';
import Title from '../../ui/title';

const Login = () => {
    // Get the environment variable for the auth API URL
    const AUTH_API_URL = process.env.AUTH_API_URL;

    // Local state for tabs, input fields, token, and login type
    const [activeTab, setActiveTab] = useState('login');
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    // token and loginType are stored in localStorage:
    // loginType: 'custom' or 'azure'
    const [token, setToken] = useState(localStorage.getItem('token') || null);
    const [loginType, setLoginType] = useState(localStorage.getItem('loginType') || null);
    const [isLoggedIn, setIsLoggedIn] = useState(!!localStorage.getItem('token'));

    // On mount, check localStorage for token and loginType.
    useEffect(() => {
        const storedToken = localStorage.getItem('token');
        const storedLoginType = localStorage.getItem('loginType');
        setToken(storedToken);
        setLoginType(storedLoginType);
        setIsLoggedIn(!!storedToken);
    }, []);

    // Check if the current URL indicates an OAuth2 success (i.e. the backend redirected after Azure login)
    useEffect(() => {
        if (window.location.pathname === '/oauth2/success') {
            // Extract token from URL if present
            const params = new URLSearchParams(window.location.search);
            const tokenFromUrl = params.get('token');
            
            if (tokenFromUrl) {
                console.log('%c Azure Authentication Success', 'background: #0078D4; color: white; padding: 2px 5px; border-radius: 3px;');
                console.log('%c JWT Token:', 'font-weight: bold; color: #0078D4;');
                console.log(tokenFromUrl);
                
                // Try to decode and display token parts
                try {
                    const [header, payload, signature] = tokenFromUrl.split('.');
                    console.log('%c Decoded Token:', 'font-weight: bold; color: #0078D4;');
                    console.log('Header:', JSON.parse(atob(header)));
                    console.log('Payload:', JSON.parse(atob(payload)));
                    console.log('Signature:', signature);
                } catch (error) {
                    console.log('Could not decode token parts:', error);
                }

                localStorage.setItem('token', tokenFromUrl);
                localStorage.setItem('loginType', 'azure');
                setToken(tokenFromUrl);
                setLoginType('azure');
                setIsLoggedIn(true);
                window.history.replaceState({}, document.title, '/');
            } else {
                // If no token in URL, try backend endpoint
                axios
                    .get(`${AUTH_API_URL}/oauth2/success`, { withCredentials: true })
                    .then(response => {
                        const jwt = response.data.token;
                        console.log('%c Azure Authentication Success', 'background: #0078D4; color: white; padding: 2px 5px; border-radius: 3px;');
                        console.log('%c JWT Token:', 'font-weight: bold; color: #0078D4;');
                        console.log(jwt);
                        
                        // Try to decode and display token parts
                        try {
                            const [header, payload, signature] = jwt.split('.');
                            console.log('%c Decoded Token:', 'font-weight: bold; color: #0078D4;');
                            console.log('Header:', JSON.parse(atob(header)));
                            console.log('Payload:', JSON.parse(atob(payload)));
                            console.log('Signature:', signature);
                        } catch (error) {
                            console.log('Could not decode token parts:', error);
                        }

                        localStorage.setItem('token', jwt);
                        localStorage.setItem('loginType', 'azure');
                        setToken(jwt);
                        setLoginType('azure');
                        setIsLoggedIn(true);
                        window.history.replaceState({}, document.title, '/');
                    })
                    .catch(error => {
                        console.error('%c Azure Authentication Error', 'background: #D4000E; color: white; padding: 2px 5px; border-radius: 3px;');
                        console.error('Error details:', error.response?.data || error.message);
                    });
            }
        }
    }, []);

    const handleLogin = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.post(`${AUTH_API_URL}/auth/login`, {
                login: username,
                password,
            });
            console.log('Custom login successful:', response.data);
            // Store token and mark as custom login
            localStorage.setItem('token', response.data.token);
            localStorage.setItem('loginType', 'custom');
            setToken(response.data.token);
            setLoginType('custom');
            setIsLoggedIn(true);
        } catch (error) {
            console.error('Login failed:', error.response?.data || error.message);
        }
    };

    const handleRegister = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.post(`${AUTH_API_URL}/auth/register`, {
                firstName,
                lastName,
                login: username,
                password,
            });
            console.log('Registration successful:', response.data);
            setActiveTab('login');
        } catch (error) {
            console.error('Registration failed:', error.response?.data || error.message);
        }
    };

    // Redirects the browser to the backend OAuth2 login URL.
    // After Azure login, the backend should redirect back to your app at /oauth2/success.
    const handleOAuth2Login = () => {
        window.location.href = `${AUTH_API_URL}/test/oauth2/login`;
    };

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('loginType');
        setToken(null);
        setLoginType(null);
        setIsLoggedIn(false);
    };

    // Determine the color of the status indicator:
    // - Blue if logged in via Azure (oauth2)
    // - Green if logged in via custom login
    // - Red otherwise
    let indicatorColor = 'red';
    if (isLoggedIn) {
        indicatorColor = loginType === 'azure' ? 'blue' : 'green';
    }

    return (
        <div className="flex flex-col items-center relative">
            {/* Status Indicator */}
            <div
                className="absolute top-4 right-4 w-4 h-4 rounded-full"
                style={{ backgroundColor: indicatorColor }}
            ></div>

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
                            onClick={handleOAuth2Login}
                            className="w-full px-4 py-2 text-white bg-gray-700 rounded-md hover:bg-gray-800 flex items-center justify-center"
                        >
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

                {/* Show Token Button */}
                <button
                    onClick={() => alert(`Token: ${token || 'No token found'}`)}
                    className="mt-4 w-full px-4 py-2 text-white bg-purple-500 rounded-md hover:bg-purple-600"
                >
                    Show Token
                </button>

                {/* Logout Button */}
                {isLoggedIn && (
                    <button
                        onClick={handleLogout}
                        className="mt-2 w-full px-4 py-2 text-white bg-red-500 rounded-md hover:bg-red-600"
                    >
                        Logout
                    </button>
                )}
            </div>
        </div>
    );
};

export default Login;

