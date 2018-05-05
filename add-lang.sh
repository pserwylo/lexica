#!/bin/bash

LANGUAGE=$1
REGION=$2

if [[ ! $LANGUAGE ]]; then
	echo "Must specify a language (and optionally a language region)."
	echo "Usage: ./add-lang.sh <lang> [<region>]"
	echo "Example: ./add-lang.sh en GB"
	exit 1
fi

if [[ $REGION ]]; then
	LANGUAGE_WITH_REGION=${LANGUAGE}_${REGION}
else
	LANGUAGE_WITH_REGION=${LANGUAGE}
fi

FOUND=`aspell dump dicts | grep ${LANGUAGE} | wc -l`

if [[ $FOUND == "0" ]]; then
	echo "Could not find language \"${LANGUAGE}\"."
	echo "Do you have the correct aspell dictionary installed?"
	echo "Check by running: aspell dump dicts"
	exit 1
fi

OUTPUT_PATH=assets/dictionaries/dictionary.${LANGUAGE_WITH_REGION}.txt

if [[ $LANGUAGE == 'fa' ]]; then
	FILTER="cat"
elif [[ $LANGUAGE == 'de' ]]; then
	FILTER="grep -P ^[[:upper:]]?\p{Ll}*$"
else
	FILTER="grep -P ^\p{Ll}*$"
fi

echo "Filter: $FILTER"

# The grep for lower case doesn't work for Farsi (and likely other languages) so I just excluded it for those languages.
#aspell -l ${LANGUAGE_WITH_REGION} dump master | aspell -l ${LANGUAGE} expand | tr ' ' '\n' | awk 'length($0) < 10 && length($0) > 2' | sort > ${OUTPUT_PATH}

# German words use capital letters for nouns, so we can't just exclude words with upper case letters.
# Instead, include words where only the first letter is upper case, and exclude those with upper case letters elsewhere.

# For English, exclude all words with any upper case letters
aspell -l ${LANGUAGE_WITH_REGION} dump master | aspell -l ${LANGUAGE} expand | tr ' ' '\n' | $FILTER | awk 'length($0) < 10 && length($0) > 2' | sort > ${OUTPUT_PATH}

echo "Wrote ${OUTPUT_PATH}"
