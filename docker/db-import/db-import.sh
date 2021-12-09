#!/bin/bash

sleep 5
echo "Importing database..."
mysql -h slytherin -u root -prainbows < db.dump
echo "Done!"
