cd Files
cd Install
cd gen

./cli.sh resources list | grep "Resource \| ip" | tr -d '\n' | sed -r 's/- Resource /\n/g' | sed '1d'