const express = require('express');
const path = require('path');

const app = express();
const PORT = 3000;

// Absolute path to your artifacts folder
const artifactsPath = path.join(__dirname, 'gen/platform/artifacts');

// Serve files
app.use('/artifacts', express.static(artifactsPath));

app.listen(PORT, () => {
  console.log(`Backend running at http://localhost:${PORT}`);
});
