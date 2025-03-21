from flask import Flask, render_template, request, redirect, url_for
from modelo import Producto

app = Flask(__name__)

# Lista global de productos
productos = [Producto("PC", 1200), Producto("Impresora", 2400)]

@app.route('/')
def inicio():
    return render_template('index.html', productos=productos)

@app.route('/editar/<nombre>/<precio>')
def editar(nombre, precio):
    return render_template('editar.html', nombre=nombre, precio=precio)

@app.route('/guardar', methods=['POST'])
def guardar():
    nombre = request.form.get('nombre') 
    precio = request.form.get('precio') 
    
   
    for producto in productos:
        if producto.nombre == nombre:
            producto.precio = int(precio)  
            print(f"Producto actualizado: {producto.nombre}, Nuevo precio: {producto.precio}")

   
    return redirect(url_for('inicio'))

@app.route('/eliminar/<nombre>')
def eliminar(nombre):
    global productos  
    productos = [producto for producto in productos if producto.nombre != nombre]
    print(f"Producto eliminado: {nombre}")
    return redirect(url_for('inicio'))

@app.route('/crear', methods=['POST'])
def crear():
    nombre = request.form.get('nombre')  
    precio = request.form.get('precio')  

    
    if not nombre or not precio:
        return "Error: Nombre o precio no pueden estar vacíos", 400  
    
    global productos  
    productos.append(Producto(nombre, int(precio))) 
    print(f"Producto creado: {nombre}, Precio: ${precio}")  

    return redirect(url_for('inicio'))  

@app.route('/productos')
def mostrar_formulario():
    return render_template('productos.html')  

if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True)