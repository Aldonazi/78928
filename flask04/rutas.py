from flask import Flask, render_template, request, redirect, url_for
import sqlite3

app = Flask(__name__)

# Función para conectar a la base de datos
def conexion():
    con = sqlite3.connect('basedatos.db')  # Base de datos SQLite
    con.row_factory = sqlite3.Row  # Para devolver resultados como diccionarios
    return con

# Función para inicializar la base de datos
def iniciar_bd():
    con = conexion()
    con.execute('''
        CREATE TABLE IF NOT EXISTS productos (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            nombre TEXT NOT NULL,
            precio REAL NOT NULL
        )
    ''')
    con.commit()
    con.close()

# Inicializa la base de datos al inicio
iniciar_bd()

# Ruta para mostrar los productos
@app.route('/')
def inicio():
    con = conexion()
    productos = con.execute('SELECT * FROM productos').fetchall()  # Obtiene todos los productos
    con.close()
    return render_template('index.html', productos=productos)

# Ruta para el formulario de creación de producto
@app.route('/productos')
def mostrar_formulario():
    return render_template('productos.html')  # Renderiza el formulario de creación

# Ruta para crear un nuevo producto
@app.route('/crear', methods=['POST'])
def crear():
    nombre = request.form.get('nombre')
    precio = request.form.get('precio')

    if not nombre or not precio:  # Validación
        return "Error: Nombre o precio no pueden estar vacíos", 400

    con = conexion()
    con.execute('INSERT INTO productos (nombre, precio) VALUES (?, ?)', (nombre, float(precio)))
    con.commit()
    con.close()
    print(f"Producto creado: {nombre}, Precio: ${precio}")
    return redirect(url_for('inicio'))

@app.route('/editar/<int:id>', methods=['GET', 'POST'])
def editar(id):
    con = conexion()
    if request.method == 'POST':  # Cuando el formulario se envía
        nombre = request.form.get('nombre')
        precio = request.form.get('precio')
        
        # Actualiza el producto en la base de datos
        con.execute('UPDATE productos SET nombre = ?, precio = ? WHERE id = ?', (nombre, float(precio), id))
        con.commit()
        con.close()
        print(f"Producto editado: ID {id}, Nuevo nombre: {nombre}, Nuevo precio: {precio}")
        return redirect(url_for('inicio'))
    else:  # Cuando se carga el formulario
        producto = con.execute('SELECT * FROM productos WHERE id = ?', (id,)).fetchone()
        con.close()
        return render_template('editar.html', producto=producto)

@app.route('/eliminar/<int:id>')
def eliminar(id):
    con = conexion()
    con.execute('DELETE FROM productos WHERE id = ?', (id,))
    con.commit()
    con.close()
    print(f"Producto eliminado: ID {id}")
    return redirect(url_for('inicio'))

if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True)