import { useState } from 'react';
import './App.css';

function App() {
  const [url, setUrl] = useState('');
  const [message, setMessage] = useState('');

  const handleSubmit = async (event) => {
    event.preventDefault();
    setMessage("Starting download process...");

    const callbackUrl = 'http://3.127.36.67:3000/api/download-link-notification'; // Define the callback URL

    try {
      const response = await fetch(`http://3.127.36.67:8080/api/start-download?url=${encodeURIComponent(url)}&callbackUrl=${encodeURIComponent(callbackUrl)}`, {
        method: 'POST',
      });

      if (response.ok) {
        const result = await response.text();
        setMessage(result);
      } else {
        setMessage("Failed to start download process.");
      }
    } catch (error) {
      console.error("Error:", error);
      setMessage("An error occurred.");
    }
  };

  return (
    <div className="App">
      <header className="App-header">
        <h1>Instagram Video Downloader</h1>
        <form onSubmit={handleSubmit}>
          <label>
            Enter Instagram URL:
            <input
              type="text"
              value={url}
              onChange={(e) => setUrl(e.target.value)}
              required
            />
          </label>
          <button type="submit">Download Video</button>
        </form>
        {message && <p>{message}</p>}
      </header>
    </div>
  );
}

export default App;
