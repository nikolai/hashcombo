# hashcombo
Hash generator that produces hashes by combinations of message digest algorithms or recovers passwords by hash.

Usage
-------------
```
Usage: <STDIN> | --algo-combo=<algorithms combination> [--target-hash=<hex>]
        Example:
        mp64 -1 ?l ?1?1?1?1?1 | java -jar HashCombo.jar --algo-combo=MD4,SHA-1 --target-hash=5c64aee6b2c51b814c7929132dec579677d175cf
```
Where 
* mp64 - a mask processor util. see https://github.com/hashcat/maskprocessor
* --algo-combo - a combination of message digest algorithm to process a STDIN with
* --target-hash - an optional target hash of the password to be found while iterating all lines in output. If this option is oitted STDOUT is filled with the result of hashing

Example output
--------------
```
> mp64 -1 ?l ?1?1?1?1?1?17 |java -jar HashGenerator.jar --algo-combo=MD4,SHA-1 --target-hash=c967d013127bdbd04ba4776639befdbfcf3d71db

progress:  current password: ilesfm7, tries: 100M, seconds spent: 34
Password cracked: psswrd7, hash: C967D013127BDBD04BA4776639BEFDBFCF3D71DB, tries: 186055667, minutes spent: 1
```
