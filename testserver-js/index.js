const express = require('express')
const counter = require('./globalCounter')
const app = express()
const port = 3000

app.get('/greet', (req, res) => {
    res.send('Hello World!')
})

app.get('/sleep/:seconds', (req, res) => {
    counter.add()
    setTimeout(() => {
        res.send('Hello World!')
        counter.dec()
    }, req.params.seconds * 1000)
})

function reportOpenRequests() {
    console.log(new Date(), "open requests " + counter.get())
    setTimeout(reportOpenRequests, 1000 * 5)
}

app.listen(port, () => {
    console.log(new Date(), `Example app listening on port ${port}`)
    reportOpenRequests()
})
