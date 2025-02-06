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

  return (
    <div className="flex flex-col items-center">
      <Logo />
      <Title className="pt-4 sm:pt-6">Section Informatique</Title>
      
      <div className="mt-8 w-full max-w-md">
        <div className="flex mb-4">
          <button
            onClick={() => setActiveTab('login')}
            className={`flex-1 py-2 px-4 text-center ${
              activeTab === 'login' ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-700'
            } rounded-l-md`}
          >
            Login
          </button>
          <button
            onClick={() => setActiveTab('register')}
            className={`flex-1 py-2 px-4 text-center ${
              activeTab === 'register' ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-700'
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
