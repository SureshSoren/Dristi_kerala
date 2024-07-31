const express = require('express');
const app = express();
const bodyParser = require('body-parser');
const caseRoutes = require('./routes/caseRoutes');

app.use(bodyParser.json());
app.use('/cases', caseRoutes);
app.use(errorHandler);

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}`);
});