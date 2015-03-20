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
    bigrams = {}
    token_count = 0
    points = []
    points.append((0,0))
    x = [0]
    y = [0]
    bigram_count = 0
    for f in files:
        tokens = tokenize(get_text(f))
        for token in tokens:
            vocab[token] = vocab.setdefault(token, 0) + 1
        for i in xrange(len(tokens)-1):
            key = (tokens[i], tokens[i+1])
            bigrams[key] = bigrams.setdefault(key, 0) + 1
            bigram_count += 1
        token_count += len(tokens)
        points.append((token_count, len(vocab)))
        x.append(token_count)
        y.append(len(vocab))
    
    # plot bigram and word frequency to rank
    d = {}
    d.update(bigrams)
    d.update(vocab)
    y = [value/(bigram_count + token_count) for key, value in d.iteritems()]
    y.sort()
    y.reverse()
    x = [i for i in xrange(1, len(y) + 1)]
    temp = [y[i-1]*i for i in x]
    k = 0.1 # sum(temp)/len(x)
    print(k)
    y2 = [k/i for i in x]
    assert len(x) == len(y)
    data_handle = plt.plot(x, y, marker='+', linestyle='None', color='black', label='Data')
    zipfs_law_handle = plt.plot(x, y2, color='blue', linestyle='-', label='Zipf')
    plt.ylabel('Log Probability')
    plt.xlabel('Log Rank')
    plt.yscale('log')
    plt.xscale('log')
    plt.title('Zipf\'s Law over Bigrams and Words\nK='+unicode(k))
    plt.show()
    
    #~ # plot bigram frequency to rank
    #~ y = [value/bigram_count for key, value in bigrams.iteritems()]
    #~ y.sort()
    #~ y.reverse()
    #~ x = [i for i in xrange(1, len(y) + 1)]
    #~ temp = [y[i-1]*i for i in x]
    #~ k = 0.04 # sum(temp)/len(x)
    #~ print(k)
    #~ y2 = [k/i for i in x]
    #~ assert len(x) == len(y)
    #~ data_handle = plt.plot(x, y, marker='+', linestyle='None', color='black', label='Data')
    #~ zipfs_law_handle = plt.plot(x, y2, color='blue', linestyle='-', label='Zipf')
    #~ plt.ylabel('Log Probability')
    #~ plt.xlabel('Log Rank')
    #~ plt.yscale('log')
    #~ plt.xscale('log')
    #~ plt.title('Zipf\'s Law over Bigrams\nK='+unicode(k))
    #~ plt.show()
    
    #~ # plot word frequency to rank
    #~ y = [value/token_count for key, value in vocab.iteritems()]
    #~ y.sort()
    #~ y.reverse()
    #~ x = [i for i in xrange(1, len(y) + 1)]
    #~ temp = [y[i-1]*i for i in x]
    #~ k = 0.1
    #~ print(k)
    #~ y2 = [k/i for i in x]
    #~ assert len(x) == len(y)
    #~ data_handle = plt.plot(x, y, marker='+', linestyle='None', color='black', label='Data')
    #~ zipfs_law_handle = plt.plot(x, y2, color='blue', linestyle='-', label='Zipf')
    #~ plt.ylabel('Log Probability')
    #~ plt.xlabel('Log Rank')
    #~ plt.yscale('log')
    #~ plt.xscale('log')
    #~ plt.title('Zipf\'s Law over Words\nK='+unicode(k))
    #~ plt.show()
