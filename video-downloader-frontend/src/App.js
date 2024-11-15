import { useState } from 'react';
import './App.css';
import config from './config';

function App() {
  const [url, setUrl] = useState('');
  const [advanced, setAdvanced] = useState(false);
  const [sourceLanguage, setSourceLanguage] = useState('en');
  const [targetLanguage, setTargetLanguage] = useState('de');
  const [message, setMessage] = useState('');
  const [downloadLink, setDownloadLink] = useState(null);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setMessage("Starting download process...");
    setDownloadLink(null);

    try {
      const params = new URLSearchParams({
        url: url,
        advanced: advanced,
        sourceLanguage: sourceLanguage,
        targetLanguage: targetLanguage,
      });
      
      const response = await fetch(`${config.apiBaseUrl}/start-download?${params}`, {
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
          <br />
          <label>
            Advanced Options:
            <input
              type="checkbox"
              checked={advanced}
              onChange={(e) => setAdvanced(e.target.checked)}
            />
          </label>
          <br />
          <label>
            Source Language:
            <select value={sourceLanguage} onChange={(e) => setSourceLanguage(e.target.value)}>
              <option value="en">English</option>
              <option value="de">German</option>
              <option value="fr">French</option>
              <option value="es">Spanish</option>
              <option value="it">Italian</option>
              <option value="pt">Portuguese</option>
              <option value="nl">Dutch</option>
              <option value="ru">Russian</option>
              <option value="zh">Chinese</option>
              <option value="ja">Japanese</option>
              <option value="ko">Korean</option>
              <option value="ar">Arabic</option>
              <option value="hi">Hindi</option>
              <option value="tr">Turkish</option>
              <option value="pl">Polish</option>
              <option value="sv">Swedish</option>
            </select>
          </label>
          <br />
          <label>
            Target Language:
            <select value={targetLanguage} onChange={(e) => setTargetLanguage(e.target.value)}>
              <option value="en">English</option>
              <option value="de">German</option>
              <option value="fr">French</option>
              <option value="es">Spanish</option>
              <option value="it">Italian</option>
              <option value="pt">Portuguese</option>
              <option value="nl">Dutch</option>
              <option value="ru">Russian</option>
              <option value="zh">Chinese</option>
              <option value="ja">Japanese</option>
              <option value="ko">Korean</option>
              <option value="ar">Arabic</option>
              <option value="hi">Hindi</option>
              <option value="tr">Turkish</option>
              <option value="pl">Polish</option>
              <option value="sv">Swedish</option>
            </select>
          </label>
          <br />
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
