import { useState } from 'react';
import './App.css';
import config from './config';

function App() {
  const [url, setUrl] = useState('');
  const [message, setMessage] = useState('');
  const [downloadLink, setDownloadLink] = useState(null);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setMessage("Starting download process...");
    setDownloadLink(null);

    try {
      const response = await fetch(`${config.apiBaseUrl}/start-download?url=${encodeURIComponent(url)}`, {
        method: 'POST',
      });

      if (response.ok) {
        const result = await response.json();
        const instanceId = result.processInstanceId;
        setMessage("Download process started. Waiting for the download link...");
        pollForDownloadLink(instanceId);
      } else {
        setMessage("Failed to start download process.");
      }
    } catch (error) {
      console.error("Error:", error);
      setMessage("An error occurred.");
    }
  };

  const pollForDownloadLink = (instanceId) => {
    const intervalId = setInterval(async () => {
      try {
        const response = await fetch(`${config.apiBaseUrl}/download-link?processInstanceId=${instanceId}`);
        
        if (response.ok) {
          const result = await response.json();
          setDownloadLink(result.downloadLink);
          setMessage("Download link is ready!");
          clearInterval(intervalId);
        } else if (response.status === 204) {
          setMessage("Waiting for download link to be ready...");
        } else {
          setMessage("Failed to retrieve download link.");
          clearInterval(intervalId);
        }
      } catch (error) {
        console.error("Error while polling for download link:", error);
        setMessage("An error occurred while waiting for the download link.");
        clearInterval(intervalId);
      }
    }, 5000);
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
        {downloadLink && (
          <a href={downloadLink} download="instagram-video.mp4">
            Click here if the download doesn’t start automatically
          </a>
        )}
      </header>
    </div>
  );
}

export default App;
