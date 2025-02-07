import React, { useState, useEffect } from 'react';
import axios from 'axios';
import Logo from '../../ui/logo';
import Title from '../../ui/title';

const Login = () => {
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
            // Call the backend endpoint to retrieve the JWT (ensure CORS and credentials are handled as needed)
            axios
                .get('http://localhost:8080/oauth2/success', { withCredentials: true })
                .then(response => {
                    const jwt = response.data.token;
                    console.log('Azure login successful, received token:', jwt);
                    localStorage.setItem('token', jwt);
                    localStorage.setItem('loginType', 'azure');
                    setToken(jwt);
                    setLoginType('azure');
                    setIsLoggedIn(true);
                    // Optionally, remove "/oauth2/success" from the URL.
                    window.history.replaceState({}, document.title, '/');
                })
                .catch(error => {
                    console.error('Error fetching Azure token:', error.response?.data || error.message);
                });
        }
    }, []);

    const handleLogin = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.post('http://localhost:8080/auth/login', {
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
            const response = await axios.post('http://localhost:8080/auth/register', {
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
        window.location.href = `http://localhost:8080/oauth2/authorization/azure`;
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

