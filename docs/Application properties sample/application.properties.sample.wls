#spring.datasource.url = jdbc:oracle:thin:@172.16.0.7:1521:GPT
#spring.datasource.username = GIB
#spring.datasource.password = GIB
spring.datasource.jndi-name = jdbc/gib

spring.jpa.show-sql = true
spring.jpa.hibernate.ddl-auto = update
#spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.Oracle10gDialect

spring.devtools.restart.trigger-file = reloadTrigger

server.session.persistent=false

#open this remark if the system want to use old EAI
#eai.server.url = http://biznetcloud:30008/EAI

gpcash.messages.preload = false

gpcash.payroll.upload.path = /home/gptadmin/app/gibcash/files/payroll/upload

# for statefull tcp-ip adapter
server.name = server1

# email properties configuration
#spring.mail.jndi-name=Session jndi name. If set overrides other properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=<user login>
spring.mail.password=<password>
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
