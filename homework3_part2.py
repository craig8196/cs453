from __future__ import division, print_function, unicode_literals
import io
import os
import re
import json
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
import numpy as np

regex = r"[a-zA-Z]+"
compiled_regex = re.compile(regex, re.UNICODE)


def get_files(directory):
    result = []
    if os.path.exists(directory):
        for f in os.listdir(directory):
            f = os.path.join(directory, f)
            if os.path.isfile(f):
                result.append(f)
    return result

def get_text(f):
    result = ''
    with io.open(f, 'r', encoding='latin-1') as g:
        result = unicode(g.read())
    return result

def tokenize(text):
    new_text = text.replace('\'', '')
    tokens = []
    for match in compiled_regex.finditer(new_text):
        tokens.append(match.group().lower())
    return tokens

if __name__ == "__main__":
    files = get_files('documents/')
    vocab = {}
    token_count = 0
    points = []
    points.append((0,0))
    x = [0]
    y = [0]
    for f in files:
        tokens = tokenize(get_text(f))
        for token in tokens:
            vocab[token] = True
        token_count += len(tokens)
        points.append((token_count, len(vocab)))
        x.append(token_count)
        y.append(len(vocab))
    
    
    k = 30
    spacing = x[-1]/300
    heaps_law_x = np.arange(0, x[-1], spacing)
    
    heaps_law_y = k*heaps_law_x**0.5
    data_handle,  = plt.plot(x, y, color='blue', label='Data')
    heaps_law_handle,  = plt.plot(heaps_law_x, heaps_law_y, color='green', label="Heap's Law")
    #~ data_patch = mpatches.Patch(color='blue', label='Data')
    #~ heap_patch = mpatches.Patch(color='green', label='Heap\'s Law')
    #~ plt.legend(handles=[data_patch])
    first_legend = plt.legend(handles=[data_handle, heaps_law_handle], loc=2)
    #~ ax = plt.gca().add_artist(first_legend)
    plt.ylabel('Words in Vocabulary')
    plt.xlabel('Words in Collection')
    plt.title('Vocabulary Growth for the Wikipedia Collection\nHeap\'s Law Parameters: k=30, beta=0.5')
    plt.show()
