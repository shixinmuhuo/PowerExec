
java -Dlog4j.configuration=file:./config/log4j.properties  \
-jar powerexec-1.0.jar \
--host_path=host.conf \
--script_path=test.script \
--max_concurrent=10
