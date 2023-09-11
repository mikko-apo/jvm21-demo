var counter = 0;

function get() {
    return counter
}

function add() {
    counter += 1
}

function dec() {
    counter -= 1
}

module.exports = {
    get, add, dec
}
