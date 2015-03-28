#! /usr/bin/env python
from __future__ import division, print_function, unicode_literals
import io
import os
import re
import math
import random
from os.path import join
# Multinomial Naive Bayes

DEBUG = True
PRINT = True

class MNBClassification(object):

    def __init__(self, training_set):
        self.training_set = training_set
        self.allowed_features = {}

    def featureSelection(self, vocab_limit=None):
        """vocab_limit -- number of vocab words to select
        self.allowed_features stores the words allowed after featureSelection is complete
        Return nothing.
        """
        MNBProbability(training_set)

    def label(self, test_set):
        """test_set -- list of documents
        Return list of classification assignments.
        """
        pass


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
            print(self.name)
            print(self.word_counts)
            print(self.total_word_count)

    def __unicode__(self):
        return unicode(self.name)

    def __str__(self):
        return str(self.name)

class Counter(object):
    
    def __init__(self, documents):
        self.original_documents = documents
        self.total_class_counts = 0
        self.total_class_word_counts = {}
        self.class_counts = {}
        self.class_word_counts = {}
        self.word_counts = {}
        self._count_all()

    def _count_all(self):
        for doc in self.original_documents:
            self.total_class_counts += 1
            classification = doc.classification
            self.class_counts[classification] = self.class_counts.setdefault(classification, 0) + 1
            for tok, tok_count in doc.word_counts.iteritems():
                if classification not in self.class_word_counts:


class MNBProbability(object):
    
    def __init__(self, training_set):
        self.training_set = training_set

    def computeWordProbability(self, allowed_features):
        """Compute probability of each word in each class C.
        Use Laplacian Smoothed Estimate."""
        pass

    def computeClassProbability(self, allowed_features):
        """Compute probability of each class in C."""
        pass

    def getWordProbability(self):
        pass

    def getClassProbability(self):
        pass

    def getWordLogProbability(self):
        pass

    def getClassLogProbability(self):
        pass

class MNBEvaluation(object):
    
    def __init__(self, mnb_probability):
        self.mnb_probability = mnb_probability

    def accuracyMeasure(self, test_set):
        """Return the accuracy measure of the labeled test documents versus their true values."""
        pass

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


    training_set = Counter(train)
    
    for i in xrange(0, 5):
        train, test = split_training_and_test(documents, 0.8, seed=0)
        training_set = Counter(train)
        for vocab_max in feature_limits:
            classifier = MNBClassifier(training_set)
            classifier.featureSelection(vocab_max)
            classifier.label(test)
        print(len(train), len(test))


