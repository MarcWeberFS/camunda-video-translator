const config = {
    apiBaseUrl: process.env.NODE_ENV === 'development'
      ? 'http://localhost:8080/api'
      : 'http://3.127.36.67:8080/api'  // Replace with your EC2 instance's IP
  };
  
  export default config;
  