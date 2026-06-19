@echo off
:: Inicia la interfaz gráfica de consultas sin mostrar la ventana de consola negra
start "" "C:\Program Files\Android\Android Studio\jbr\bin\javaw.exe" -cp ".;mysql-connector-j-9.0.0.jar" QueryGui
exit
