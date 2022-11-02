#!/bin/bash
sleep 30
sudo apt-get update
sudo apt-get upgrade -y
sudo apt-get install apache2 -y
sudo apt-get clean

sudo apt install tomcat9 tomcat9-admin -y
sudo apt install openjdk-11-jre-headless -y
#sudo apt install mysql-server -y
sudo apt install maven -y
sudo ufw allow from any to any port 8080 proto tcp
#sudo variable1=$(echo "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'root'; exit" | sudo mysql)
pwd
ls -la
sudo iptables -A PREROUTING -t nat -p tcp --dport 80 -j REDIRECT --to-ports 8080
sudo mv /tmp/SpringBootApp-0.0.1-SNAPSHOT.war ~/SpringBootApp-0.0.1-SNAPSHOT.war
sudo mv ~/SpringBootApp-0.0.1-SNAPSHOT.war ~/SpringBootApp.war
sudo rm -rf /var/lib/tomcat9/webapps/ROOT
sudo cp ~/SpringBootApp.war /var/lib/tomcat9/webapps
sudo mv /var/lib/tomcat9/webapps/webapp.war /var/lib/tomcat9/webapps/ROOT.war
sudo iptables -A PREROUTING -t nat -p tcp --dport 80 -j REDIRECT --to-ports 8080