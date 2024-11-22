#!/bin/bash

# Comprueba si el directorio actual ya es un repositorio Git
if [ ! -d ".git" ]; then
    # Si no es un repositorio Git, inicializa uno
    git init
    echo "Repositorio Git inicializado."
else
    echo "Repositorio Git ya existe en este directorio."
fi

# Solicita el nombre del repositorio en GitHub
read -p "Introduce el nombre del repositorio en GitHub: " repo_name

# Configura el nombre y correo del usuario de Git
git config user.name "enriqueArranz"
git config user.email "enrique.arrantzale@gmail.com"

# Verifica si el repositorio existe en GitHub, si no lo crea
if ! gh repo view "EnriqueArranz/$repo_name" &>/dev/null; then
    # Si no existe, crea el repositorio en GitHub
    gh repo create "EnriqueArranz/$repo_name" --public
    echo "Repositorio '$repo_name' creado en GitHub."
else
    echo "El repositorio '$repo_name' ya existe en GitHub."
fi

# Añade todos los archivos al índice de Git
git add .

# Realiza el primer commit
git commit -m "Primer commit"

# Añade el remoto de GitHub
git remote add origin "https://github.com/EnriqueArranz/$repo_name.git"

# Realiza el primer push al repositorio en GitHub
git push -u origin master || echo "Error: No se pudo hacer push al repositorio. Verifica que el repositorio existe en GitHub y la URL es correcta."

# Pausa al final del script para que el usuario vea el resultado
read -p "Presiona [Enter] para cerrar..."