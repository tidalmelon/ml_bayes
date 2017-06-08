#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
Date: 2017/06/08 09:51:35
朴素贝叶斯分类器
"""
import os
import json
import math

FILE_TRAIN = 'train.dat'
FILE_TEST = 'test.dat'
FILE_MODEL = 'model.dat'

CLASS_PROB_DIC = {}
CLASS_FEA_PROB_DIC = {}
CLASS_DEFAULT_PROB_DIC = {}
FEADIC = {}

def trainModel(ftrain):

    DEFAULT_COUNT = 0.1
    CLASS_COUNT_DIC = {}
    CLASS_FEA_COUNT_DIC = {}

    with open(ftrain) as f:
        while True:
            line = f.readline()
            if not line:
                break
            line = line.strip()
            arr = line.split()
            classid = arr[0]

            if classid not in CLASS_COUNT_DIC:
                CLASS_COUNT_DIC[classid] = 1
            else:
                CLASS_COUNT_DIC[classid] += 1

            for fea in arr[1:]:
                fea = int(fea)
                if fea not in FEADIC:
                    FEADIC[fea] = 1

                if classid not in CLASS_FEA_COUNT_DIC:
                    CLASS_FEA_COUNT_DIC[classid] = {}
                    CLASS_FEA_COUNT_DIC[classid][fea] = 1
                else:
                    if fea not in CLASS_FEA_COUNT_DIC[classid]:
                        CLASS_FEA_COUNT_DIC[classid][fea] = 1
                    else:
                        CLASS_FEA_COUNT_DIC[classid][fea] += 1


    CLASS_SUM = sum(CLASS_COUNT_DIC.values())
    for classid, count in CLASS_COUNT_DIC.iteritems():
        CLASS_PROB_DIC[classid] = float(count) / CLASS_SUM

    for classid, fea_dic in CLASS_FEA_COUNT_DIC.iteritems():
        CLASS_FEA_PROB_DIC[classid] = {}

        FEA_SUM = sum(fea_dic.itervalues())
        # 多项式分布
        FEA_SUM += len(FEADIC) * DEFAULT_COUNT
        # 二项分布
        #FEA_SUM = CLASS_COUNT_DIC[classid] + 2 * DEFAULT_COUNT

        for fea, count in fea_dic.iteritems():
            CLASS_FEA_PROB_DIC[classid][fea] = float(count + DEFAULT_COUNT) / FEA_SUM
        CLASS_DEFAULT_PROB_DIC[classid] = DEFAULT_COUNT / FEA_SUM


def saveModel():
    class_prob_json = json.dumps(CLASS_PROB_DIC)
    class_default_prob_json = json.dumps(CLASS_DEFAULT_PROB_DIC)
    class_fea_prob_json = json.dumps(CLASS_FEA_PROB_DIC)
    feajson = json.dumps(FEADIC)
    with open(FILE_MODEL, 'w') as f:
        f.write(class_prob_json + os.linesep)
        f.write(class_default_prob_json + os.linesep)
        f.write(class_fea_prob_json + os.linesep)
        f.write(feajson + os.linesep)


def loadModel():
    with open(FILE_MODEL, 'w') as f:
        class_prob_json = f.readline().strip()
        class_default_prob_json = f.readline().strip()
        class_fea_prob_json = f.readline().strip()
        feajson = f.readline().strip()
        global CLASS_PROB_DIC
        global CLASS_DEFAULT_PROB_DIC
        global CLASS_FEA_PROB_DIC
        global FEADIC
        CLASS_PROB_DIC = json.loads(class_prob_json)
        CLASS_DEFAULT_PROB_DIC = json.loads(class_default_prob_json)
        CLASS_FEA_PROB_DIC = json.loads(class_fea_prob_json)
        FEADIC = json.loads(feajson)
    print len(CLASS_PROB_DIC), 'classes!', len(FEADIC), 'features!'

def predict(ftest):
    global CLASS_PROB_DIC
    global CLASS_FEA_PROB_DIC
    global CLASS_DEFAULT_PROB_DIC
    global FEADIC
    
    REAL_PRED_LIST = []
    with open(ftest) as f:
        while True:
            line = f.readline()
            if not line:
                break
            arr = line.strip().split()
            realclassname = arr[0]

            class_score_dic = {}
            for classname, prob in CLASS_PROB_DIC.items():
                class_score_dic[classname] = math.log(prob)

                #for fea in arr[1:]:
                #    fea = int(fea)
                #    for classname in CLASS_PROB_DIC:
                #        if fea not in CLASS_FEA_PROB_DIC[classname]:
                #            class_score_dic[classname] += math.log(CLASS_DEFAULT_PROB_DIC[classname])
                #        else:
                #            class_score_dic[classname] += math.log(CLASS_FEA_PROB_DIC[classname][fea])

            for fea in arr[1:]:
                fea = int(fea)
                for classname in CLASS_PROB_DIC:
                    if fea not in CLASS_FEA_PROB_DIC[classname]:
                        class_score_dic[classname] += math.log(CLASS_DEFAULT_PROB_DIC[classname])
                    else:
                        class_score_dic[classname] += math.log(CLASS_FEA_PROB_DIC[classname][fea])

            maxprob = max(class_score_dic.itervalues())
            for classname, score in class_score_dic.items():
                if score == maxprob:
                    REAL_PRED_LIST.append((realclassname, classname))
    return REAL_PRED_LIST


def evalute(REAL_PRED_LIST):
    accuracy = 0
    for r, p in REAL_PRED_LIST:
        if r == p:
            accuracy += 1
    accuracy = float(accuracy) / len(REAL_PRED_LIST)
    print 'Accuracy:', accuracy


def calcPreRec(REAL_PRED_LIST, classname):
    correct = 0
    all = 0
    pred = 0

    for r, p in REAL_PRED_LIST:
        if r == classname:
            all += 1
            if r == p:
                correct += 1
        if p == classname:
            pred += 1

    return float(correct) / pred, float(correct) / all


trainModel(FILE_TRAIN)
R_P_LIST = predict(FILE_TEST)
evalute(R_P_LIST)
for classname in CLASS_PROB_DIC:
    pre, rec = calcPreRec(R_P_LIST, classname)
    print 'Precision and recall for class', classname, ':', pre, rec






























