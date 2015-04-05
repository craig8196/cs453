#! /usr/bin/env python
from __future__ import division, print_function, unicode_literals
import io
import os
import re
import math
import time
import random
from os.path import join, isdir
# Multinomial Naive Bayes (MNB)

DEBUG = False
PRINT = False
PRINT_RESULTS = True
PRINT_INFO = False

def avg(l):
    """Return the average of a list of numbers."""
    return sum(l)/len(l)


class MNBClassification(object):

    def __init__(self, training_set):
        self.training_set = training_set
        self.selected_features = None

    def featureSelection(self, vocab_limit=None):
        """vocab_limit -- number of vocab words to select
        self.allowed_features stores the words allowed after featureSelection is complete
        Return nothing.
        """
        if vocab_limit is None:
            self.selected_features = None
        else:
            # num docs
            c_docs = len(self.training_set)
            # num docs labeled as c
            c_c = {}
            # num docs containing feature w
            c_w = {}
            # num docs labeled c containing w
            c_c_given_w = {}
            # information gain words
            ig_w = {}
            
            # populate the counts
            for doc in self.training_set:
                c = doc.classification
                c_c[c] = c_c.setdefault(c, 0) + 1
                words = doc.word_counts
                for w in words:
                    c_w[w] = c_w.setdefault(w, 0) + 1
                    if w not in c_c_given_w:
                        c_c_given_w[w] = {}
                    classes = c_c_given_w[w]
                    classes[c] = classes.setdefault(c, 0) + 1
                    ig_w[w] = 0 # initialize all words to zero
            
            if PRINT_INFO: print("Vocab Size:", len(ig_w))
            
            # probability functions
            def prob_c(c):
                return c_c.setdefault(c, 0)/c_docs
            def prob_w(w):
                return c_w.setdefault(w, 0)/c_docs
            def prob_not_w(w):
                return (c_docs - c_w.setdefault(w, 0))/c_docs
            def prob_c_given_w(c, w):
                numerator = c_c_given_w[w].setdefault(c, 0)
                return numerator/c_w.setdefault(w, numerator)
            def prob_c_given_not_w(c, w):
                num_in_c = c_c.setdefault(c, 0)
                numerator = num_in_c - c_c_given_w[w].setdefault(c, 0)
                denominator = c_docs - c_w.setdefault(w, 0)
                if denominator == 0.0: return 0
                if DEBUG: assert numerator/denominator <= 1.0
                return numerator/denominator
            
            # compute scores
            for w in ig_w:
                if PRINT: print("IG Word:", w)
                first_sum = 0
                second_sum = 0
                third_sum = 0
                for c in c_c:
                    p = prob_c(c)
                    if PRINT: print("P(c):", "P(%s)="%(c), p)
                    if p > 0: first_sum += p*math.log(p, 2)
                    p = prob_c_given_w(c, w)
                    if PRINT: print("P(c|w):", "P(%s|%s)="%(c,w), p)
                    if p > 0: second_sum += p*math.log(p, 2)
                    p = prob_c_given_not_w(c, w)
                    if PRINT: print("P(c|not w):", "P(%s|not %s)="%(c,w), p)
                    if p > 0: third_sum += p*math.log(p, 2)
                if PRINT:
                    print("P(w):", "P(%s)="%(w), prob_w(w))
                    print("P(not w):", "P(not %s)="%(w), prob_not_w(w))
                    print("First Term:", -first_sum)
                    print("Second Term:", prob_w(w)*second_sum)
                    print("Third Term:", prob_not_w(w)*third_sum)
                ig_w[w] = -first_sum + prob_w(w)*second_sum + prob_not_w(w)*third_sum
            
            if PRINT: print("IG:", ig_w)
            
            # select features
            feature_scores = [ (w, score) for w, score in ig_w.iteritems() ]
            feature_scores = sorted(feature_scores, key=lambda x: x[1], reverse=True)
            selected_features = { w: True for w, score in feature_scores[0:vocab_limit] }
            
            if PRINT: print("Selected Features:", selected_features)
            self.selected_features = selected_features
    
    def train(self):
        self.probabilities = MNBProbability(self.training_set)
        self.probabilities.computeWordProbability(self.selected_features)
        self.probabilities.computeClassProbability()
        if PRINT: self.probabilities.pretty_print()

    def label(self, test_set):
        """test_set -- list of documents
        Return list of class/label assignments.
        """
        results = []
        for test_doc in test_set:
            max_prob = 0
            max_class = None
            for c in self.probabilities.class_probabilities:
                prob = self.probabilities.getClassLogProbability(c)
                for word, word_count in test_doc.word_counts.iteritems():
                    if self.selected_features is None or word in self.selected_features:
                        prob += word_count*self.probabilities.getWordLogProbability(word, c)
                if max_class is None or max_prob < prob:
                    max_prob = prob
                    max_class = c
            results.append(max_class)
            if PRINT:
                test_doc.pretty_print()
                print("Classified as:", max_class)
                print("Probability:", 2**max_prob)
                print()
            
        return results
                


class Document(object):
    
    def __init__(self, file_name, classification, tokens):
        self.name = file_name
        self.classification = classification
        self.word_counts = {}
        self.total_word_count = 0
        self.add_tokens(tokens)
        if PRINT: self.pretty_print()

    def add_tokens(self, tokens):
        for token in tokens:
            self.word_counts[token] = self.word_counts.setdefault(token, 0) + 1
            self.total_word_count += 1
    
    def pretty_print(self):
        print("Name:", self)
        print("Class:", self.classification)
        print("Total Word Count:", self.total_word_count)
        print("Word Counts:", self.word_counts)
        print()

    def __unicode__(self):
        return unicode(self.name)

    def __str__(self):
        return str(self.name)
    
    def __repr__(self):
        return unicode(self)

class MNBProbability(object):
    
    def __init__(self, documents):
        self.original_documents = documents

    #~ def computeWordProbability(self, selected_features=None):
        #~ """Compute probability of each word in each class C.
        #~ Use Laplacian Smoothed Estimate."""
        #~ self.word_probabilities = {}
        #~ self.junk_word_probabilities = {}
        #~ word_counts = {}
        #~ total_word_counts = {}
        #~ vocab = {}
        #~ for doc in self.original_documents:
            #~ classification = doc.classification
            #~ if classification not in word_counts:
                #~ word_counts[classification] = {}
            #~ w_counts = word_counts[classification]
            #~ total_words_added = 0
            #~ for word, word_count in doc.word_counts.iteritems():
                #~ if selected_features is None or word in selected_features:
                    #~ vocab[word] = True
                    #~ w_counts[word] = w_counts.setdefault(word, 0) + word_count
                    #~ total_words_added += word_count
            #~ total_word_counts[classification] = total_word_counts.setdefault(classification, 0) + total_words_added
        #~ 
        #~ for c in total_word_counts:
            #~ denominator = total_word_counts[c] + len(vocab)
            #~ words = word_counts[c]
            #~ word_probs = {}
            #~ for word, word_count in words.iteritems():
                #~ word_count += 1
                #~ word_probs[word] = word_count/denominator
            #~ self.word_probabilities[c] = word_probs
            #~ self.junk_word_probabilities[c] = 1/denominator
        #~ 
        #~ self.word_counts = word_counts
        #~ self.total_word_counts = total_word_counts
#~ 
    #~ def computeClassProbability(self):
        #~ """Compute probability of each class in C."""
        #~ self.class_probabilities = {}
        #~ class_counts = {}
        #~ total_classes = len(self.original_documents)
        #~ for doc in self.original_documents:
            #~ c = doc.classification
            #~ class_counts[c] = class_counts.setdefault(c, 0) + 1
        #~ for c, count in class_counts.iteritems():
            #~ self.class_probabilities[c] = count/total_classes
        #~ self.class_counts = class_counts
        #~ self.total_classes = total_classes
    #~ 
    #~ def pretty_print(self):
        #~ print("Total Classes:", self.total_classes)
        #~ print("Class Counts:", self.class_counts)
        #~ print("Total Word Counts by Class:", self.total_word_counts)
        #~ print("Word Counts by Class:", self.word_counts)
        #~ print()
#~ 
    #~ def getWordProbability(self, w, given_c):
        #~ word_probs = self.word_probabilities[given_c]
        #~ if w not in word_probs:
            #~ return self.junk_word_probabilities[given_c]
        #~ else:
            #~ return word_probs[w]
#~ 
    #~ def getClassProbability(self, c):
        #~ return self.class_probabilities.setdefault(c, 0)
#~ 
    #~ def getWordLogProbability(self, w, given_c):
        #~ return math.log(self.getWordProbability(w, given_c), 2)
#~ 
    #~ def getClassLogProbability(self, c):
        #~ return math.log(self.getClassProbability(c), 2)
    
    def computeWordProbability(self, selected_features=None):
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
                if selected_features is None or word in selected_features:
                    vocab[word] = True
                    w_counts[word] = w_counts.setdefault(word, 0) + word_count
                    total_words_added += word_count
            total_word_counts[classification] = total_word_counts.setdefault(classification, 0) + total_words_added
        
        for c in total_word_counts:
            denominator = total_word_counts[c] + len(vocab)
            words = word_counts[c]
            word_probs = {}
            for word, word_count in words.iteritems():
                word_count += 1
                word_probs[word] = math.log(word_count/denominator, 2)
            self.word_probabilities[c] = word_probs
            self.junk_word_probabilities[c] = math.log(1/denominator, 2)
        
        self.word_counts = word_counts
        self.total_word_counts = total_word_counts

    def computeClassProbability(self):
        """Compute probability of each class in C."""
        self.class_probabilities = {}
        class_counts = {}
        total_classes = len(self.original_documents)
        for doc in self.original_documents:
            c = doc.classification
            class_counts[c] = class_counts.setdefault(c, 0) + 1
        for c, count in class_counts.iteritems():
            self.class_probabilities[c] = math.log(count/total_classes, 2)
        self.class_counts = class_counts
        self.total_classes = total_classes
    
    def pretty_print(self):
        print("Total Classes:", self.total_classes)
        print("Class Counts:", self.class_counts)
        print("Total Word Counts by Class:", self.total_word_counts)
        print("Word Counts by Class:", self.word_counts)
        print()

    def getWordProbability(self, w, given_c):
        return 2**self.getWordLogProbability(w, given_c)

    def getClassProbability(self, c):
        return 2**self.getClassLogProbability(c)

    def getWordLogProbability(self, w, given_c):
        word_probs = self.word_probabilities[given_c]
        if w not in word_probs:
            return self.junk_word_probabilities[given_c]
        else:
            return word_probs[w]

    def getClassLogProbability(self, c):
        return self.class_probabilities.setdefault(c, 0)

class MNBEvaluation(object):
    
    def __init__(self):
        self.feature_selection_times = []
        self.training_times = []
        self.testing_times = []
        self.accuracies = []
    
    def trainingTimeMeasure(self, training_set, vocab_max=None):
        """Store the vocab selection and training time."""
        mnb = MNBClassification(training_set)
        
        start = time.time()
        selected_features = mnb.featureSelection(vocab_max)
        self.feature_selection_times.append(time.time() - start)
        
        start = time.time()
        mnb.train()
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
def get_tokens_from_file(file_name, stopwords, compiled_regex=COMPILED_REGEX, remove_header=False):
    tokens = []
    with io.open(file_name, 'r', encoding='utf-8', errors='ignore') as f:
        text = f.read()
        if remove_header:
            text = text.split('\n\n', 1)[1]
        for match in compiled_regex.finditer(text):
            token = match.group().lower()
            if token not in stopwords:
                tokens.append(token)
    return tokens

def get_test_documents():
    directory = 'test_documents'
    stopwords = set()
    documents = []
    spam = set([2, 4, 5])
    classifications = { True: "spam", False: "not spam" }
    for i in xrange(1, 11):
        file_name = join(directory, str(i))
        classification = classifications[i in spam]
        tokens = get_tokens_from_file(file_name, stopwords)
        document = Document(file_name, classification, tokens)
        documents.append(document)
    return documents


def get_20NG_documents(use_broader_class=False):
    documents = []
    root = 'documents'
    # get stopwords
    stopwords = set(get_tokens_from_file('stopwords.txt', set()))
    # get directories in documents directory
    classes = [d for d in os.listdir(root) if isdir(join(root, d))]
    # get tokens and create documents
    for c in classes:
        count = 0
        classification = c
        if use_broader_class:
            classification = c.split('.')[0]
        for file_name in os.listdir(join(root, c)):
            count += 1
            f = join(root, c, file_name)
            tokens = get_tokens_from_file(f, stopwords, remove_header=True)
            document = Document(f, classification, tokens)
            documents.append(document)
        if PRINT_INFO:
            print("Dir:", c)
            print("Count:", count)
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
    if DEBUG: assert len(documents) == len(training) + len(test)
    return (training, test)


if __name__ == "__main__":
    #~ documents = get_20NG_documents(True)
    documents = get_20NG_documents(False)
    #~ documents = get_test_documents()
    
    if PRINT_INFO: print("Number of Documents:", len(documents))
    
    feature_limits = [6200, 12400, 18600, 24800, None]
    #~ feature_limits = [None]
    #~ feature_limits = [None, 6200]
    
    validation_iterations = 5
    #~ validation_iterations = 1
    
    statistics = []
    
    pairs = []
    for i in xrange(0, validation_iterations):
        pairs.append(split_training_and_test(documents, 0.8, seed=None))
    
    for vocab_max in feature_limits:
        evaluation = MNBEvaluation()
        #~ for i in xrange(0, validation_iterations):
            #~ training_set, test_set = split_training_and_test(documents, 0.8, seed=None)
        for training_set, test_set in pairs:
            evaluation.trainingTimeMeasure(training_set, vocab_max)
            evaluation.accuracyMeasure(test_set)
        if PRINT_RESULTS: print(unicode(vocab_max) + ', ', ', '.join([unicode(x) for x in evaluation.getAverages()]))
        statistics.append(evaluation.getAverages())
    

