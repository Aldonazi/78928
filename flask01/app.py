from flask import Flask

app = Flask(__name__)

@app.route('/')
def hola_mundo():
    return "hola mundo"

@app.route('/adios')
def adios_mundo():
    return "adios mundo"

@app.route('/hola')
def hola_html():
    return "<h1 style=color:red>hola html</h1>"

@app.route('/json')
def algo():
    return '{"nombre":"john"}'

@app.route('/xml')
def xml():
    return '<nombre>john</nombre>'

if __name__ == "__main__":
    app.run(host='0.0.0.0', debug=True)