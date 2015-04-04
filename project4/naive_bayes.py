#! /usr/bin/env python
from __future__ import division, print_function, unicode_literals
import io
import os
import re
import math
import time
import random
from os.path import join
# Multinomial Naive Bayes

DEBUG = True
PRINT = True

def avg(l):
    return sum(l)/len(l)


class MNBClassification(object):

    def __init__(self, training_set):
        self.training_set = training_set
        self.allowed_features = {}

    def featureSelection(self, vocab_limit=None):
        """vocab_limit -- number of vocab words to select
        self.allowed_features stores the words allowed after featureSelection is complete
        Return nothing.
        """
        self.probabilities = MNBProbability(self.training_set)
        self.probabilities.computeWordProbability(set())
        self.probabilities.computeClassProbability()
        if vocab_limit is not None:
            pass
    
    def train(self, selected_features):
        pass

    def label(self, test_set):
        """test_set -- list of documents
        Return list of classification assignments.
        """
        results = []
        for test_doc in test_set:
            max_prob = 0
            max_class = None
            for c in self.probabilities.class_probabilities:
                prob = self.probabilities.getClassLogProbability(c)
                for word, word_count in test_doc.word_counts.iteritems():
                    prob += word_count*self.probabilities.getWordLogProbability(word, c)
                if max_class is None or max_prob < prob:
                    max_prob = prob
                    max_class = c
            results.append(c)
        return results
                


class Document(object):
    
    def __init__(self, file_name, classification, tokens):
        self.name = file_name
        self.classification = classification
        self.word_counts = {}
        self.total_word_count = 0
        self.add_tokens(tokens)

    def add_tokens(self, tokens):
        for token in tokens:
            self.word_counts[token] = self.word_counts.setdefault(token, 0) + 1
            self.total_word_count += 1
        if PRINT:
            print(self)
            print(self.word_counts)
            print(self.total_word_count)

    def __unicode__(self):
        return unicode(self.name)

    def __str__(self):
        return str(self.name)
    
    def __repr__(self):
        return unicode(self)

#~ class Counter(object):
    #~ 
    #~ def __init__(self, documents):
        #~ self.original_documents = documents
        #~ self.total_class_counts = 0 # total number of classes (equals to total number of documents)
        #~ self.total_class_word_counts = {} # total number of tokens in each class
        #~ self.class_counts = {} # 
        #~ self.class_word_counts = {}
        #~ self.word_counts = {}
        #~ self._count_all()
#~ 
    #~ def _count_all(self):
        #~ for doc in self.original_documents:
            #~ self.total_class_counts += 1
            #~ classification = doc.classification
            #~ self.class_counts[classification] = self.class_counts.setdefault(classification, 0) + 1
            #~ for tok, tok_count in doc.word_counts.iteritems():
                #~ if classification not in self.class_word_counts:


class MNBProbability(object):
    
    def __init__(self, documents):
        self.original_documents = documents

    def computeWordProbability(self, allowed_features):
        """Compute probability of each word in each class C.
        Use Laplacian Smoothed Estimate."""
        self.word_probabilities = {}
        self.junk_word_probabilities = {}
        word_counts = {}
        total_word_counts = {}
        vocab = {}
        for doc in self.original_documents:
            classification = doc.classification
            if classification not in word_counts:
                word_counts[classification] = {}
            w_counts = word_counts[classification]
            total_words_added = 0
            for word, word_count in doc.word_counts.iteritems():
                if word in allowed_features or not allowed_features:
                    vocab[word] = True
                    w_counts[word] = w_counts.setdefault(word, 0) + word_count
                    total_words_added += word_count
            total_word_counts[classification] = total_word_counts.setdefault(classification, 0) + total_words_added
        
        print(word_counts)
        print(total_word_counts)
        
        for c in total_word_counts:
            denominator = total_word_counts[c] + len(vocab)
            words = word_counts[c]
            word_probs = {}
            for word, word_count in words.iteritems():
                word_count += 1
                word_probs[word] = word_count/denominator
            self.word_probabilities[c] = word_probs
            self.junk_word_probabilities[c] = 1/denominator
        print(self.word_probabilities)

    def computeClassProbability(self):
        """Compute probability of each class in C."""
        self.class_probabilities = {}
        class_counts = {}
        total_classes = len(self.original_documents)
        for doc in self.original_documents:
            c = doc.classification
            class_counts[c] = class_counts.setdefault(c, 0) + 1
        for c, count in class_counts.iteritems():
            self.class_probabilities[c] = count/total_classes
        print(self.class_probabilities)

    def getWordProbability(self, w, given_c):
        word_probs = self.word_probabilities[given_c]
        if w not in word_probs:
            return self.junk_word_probabilities[given_c]
        else:
            return word_probs[w]

    def getClassProbability(self, c):
        return self.class_probabilities.setdefault(c, 0)

    def getWordLogProbability(self, w, given_c):
        return math.log(self.getWordProbability(w, given_c), 2)

    def getClassLogProbability(self, c):
        return math.log(self.getClassProbability(c), 2)
    
    #~ def getNonZeroClassLogProbability(self, c):
        #~ p = self.getClassProbability(c)
        #~ if p <= 0:
            #~ return 1.0
        #~ else:
            #~ return p

class MNBEvaluation(object):
    
    def __init__(self):
        self.feature_selection_times = []
        self.training_times = []
        self.testing_times = []
        self.accuracies = []
    
    def trainingTimeMeasure(self, training_set, vocab_max=None):
        """Time the duration of the vocab selection and training time."""
        mnb = MNBClassification(training_set)
        
        start = time.time()
        if vocab_max is None:
            selected_features = None
        else:
            selected_features = mnb.featureSelection(vocab_max)
        self.feature_selection_times.append(time.time() - start)
        
        start = time.time()
        mnb.train(selected_features)
        self.training_times.append(time.time() - start)
        
        self.mnb = mnb
    
    def accuracyMeasure(self, test_set):
        """Store the accuracy measure of the labeled test documents versus their true values."""
        start = time.time()
        results = self.mnb.label(test_set)
        self.testing_times.append(time.time() - start)
        
        number_correct = 0
        for doc, label in zip(test_set, results):
            if doc.classification == label:
                number_correct += 1
        self.accuracies.append(number_correct/len(test_set))
    
    def getAverages(self):
        """Return average (feature_selection_time, training_time, test_time, test_accuracy)."""
        avg_feature_selection_time = avg(self.feature_selection_times)
        avg_training_time = avg(self.training_times)
        avg_testing_time = avg(self.testing_times)
        avg_accuracy = avg(self.accuracies)
        return (avg_feature_selection_time, avg_training_time, avg_testing_time, avg_accuracy)

COMPILED_REGEX = re.compile(r"[a-zA-Z']+", re.UNICODE)
def get_tokens_from_file(file_name, stopwords, compiled_regex=COMPILED_REGEX):
    tokens = []
    with io.open(file_name, 'r', encoding='utf-8', errors='ignore') as f:
        for match in compiled_regex.finditer(f.read()):
            token = match.group().lower()
            if token not in stopwords:
                tokens.append(token)
    return tokens

def get_test_documents():
    directory = 'test_documents'
    stopwords = set()
    documents = []
    spam = set([2, 4, 5])
    for i in xrange(1, 11):
        file_name = join(directory, str(i))
        classification = i in spam
        tokens = get_tokens_from_file(file_name, stopwords)
        document = Document(file_name, classification, tokens)
        documents.append(document)
    return documents


def get_20NG_documents():
    # get folders in documents
    
    # get file names of the documents
    # get stopwords
    stopwords = set(get_tokens_from_file(f, set()))
    # create the document classes
    documents = []
    for c, f in files:
        tokens = get_tokens_from_file(f, stopwords)
        document = Document(f, c, tokens)
        documents.append(document)
    return documents

def split_training_and_test(documents, percent_training, seed=None):
    training = []
    test = []
    number_training = math.floor(len(documents)*percent_training)
    random.seed(seed)
    indices = [] # indices of documents for training
    for index in xrange(len(documents)):
        if index < number_training:
            indices.append(index)
        else:
            r = random.randint(0, index)
            if r < number_training:
                indices[r] = index
    indices = set(indices)
    for i, document in enumerate(documents):
        if i in indices:
            training.append(document)
        else:
            test.append(document)
    assert len(documents) == len(training) + len(test)
    return (training, test)

if __name__ == "__main__":
    # documents = get_20NG_documents()

    documents = get_test_documents()
    feature_limits = [None, 6200, 12400, 18600, 24800]
    
    
    for vocab_max in feature_limits:
        evaluation = MNBEvaluation()
        for i in xrange(0, 5):
            training_set, test_set = split_training_and_test(documents, 0.8, seed=0)
            training_set = documents
            test_set = documents
            evaluation.trainingTimeMeasure(training_set, vocab_max)
            evaluation.accuracyMeasure(test_set)
        print(evaluation.getAverages())


