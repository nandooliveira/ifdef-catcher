docker compose down
rm -rf CPPTSTATS
rm -rf temp
rm -rf target
sbt assembly
rm -rf ifdef-catcher-assembly-0.1.jar
cp target/scala-2.13/ifdef-catcher-assembly-0.1.jar ./
docker compose up
