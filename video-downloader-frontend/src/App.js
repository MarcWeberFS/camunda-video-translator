import { useState, useEffect } from 'react';
import './App.css';

function App() {
  const [url, setUrl] = useState('');
  const [message, setMessage] = useState('');
  const [downloadLink, setDownloadLink] = useState(null);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setMessage("Starting download process...");
    setDownloadLink(null);

    try {
      const response = await fetch(`http://3.127.36.67:8080/api/start-download?url=${encodeURIComponent(url)}`, {
        method: 'POST',
      });

      if (response.ok) {
        const result = await response.text();
        setMessage(result);
        pollForDownloadLink();
      } else {
        setMessage("Failed to start download process.");
      }
    } catch (error) {
      console.error("Error:", error);
      setMessage("An error occurred.");
    }
  };

  const pollForDownloadLink = async () => {
    const intervalId = setInterval(async () => {
      try {
        const response = await fetch(`http://3.127.36.67:8080/api/get-download-link?processInstanceId=YOUR_PROCESS_INSTANCE_ID`);
        if (response.ok) {
          const result = await response.json();
          if (result.downloadLink) {
            setDownloadLink(result.downloadLink);
            setMessage("Download link is ready!");
            clearInterval(intervalId); // Stop polling once the link is available
          }
        } else if (response.status === 204) {
          setMessage("Waiting for download link to be ready...");
        }
      } catch (error) {
        console.error("Error while polling for download link:", error);
        setMessage("An error occurred while waiting for the download link.");
        clearInterval(intervalId);
      }
    }, 5000); // Poll every 5 seconds
  };

  useEffect(() => {
    if (downloadLink) {
      // Automatically trigger the download
      const a = document.createElement('a');
      a.href = downloadLink;
      a.download = 'instagram-video.mp4';
      a.click();
    }
  }, [downloadLink]);

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
