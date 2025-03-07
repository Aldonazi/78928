from flask import Flask, render_template
from modelo import producto
app =Flask (__name__)

@app.route('/')

def inicio ():
    producto = [producto("manzana ", 12), producto("pera ", 24)]
    return render_template('index.html', producto=producto)
if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True)