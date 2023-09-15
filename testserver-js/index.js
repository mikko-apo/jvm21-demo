const express = require('express')
const counter = require('./globalCounter')
const app = express()
const port = 3000

app.get('/greet', (req, res) => {
    res.send('Hello World!')
})

app.get('/sleep/:seconds', (req, res) => {
    counter.add()
    const seconds = req.params.seconds;
    setTimeout(() => {
        res.send('Slept '+seconds+' seconds!')
        counter.dec()
    }, seconds * 1000)
})

function reportOpenRequests() {
    console.log(new Date(), "open requests " + counter.get())
    setTimeout(reportOpenRequests, 1000 * 5)
}

app.listen(port, () => {
    console.log(new Date(), `Example app listening on port ${port}`)
    reportOpenRequests()
})
