sleep 5

echo "Importing database..."

mysql -h slytherin -u bits_vanilla -prainbows < db.dump

echo "Done!"
