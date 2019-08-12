from numpy.random import seed
seed(123)
from tensorflow import set_random_seed
set_random_seed(123)
from numpy.random import seed
seed(123)
from tensorflow import set_random_seed
set_random_seed(123)

import numpy as np
np.seterr(divide='ignore', invalid='ignore')

from itertools import combinations
import itertools
import numpy as np
import pandas as pd
from sklearn.preprocessing import MinMaxScaler, StandardScaler, LabelBinarizer
from numpy import array
from sklearn.preprocessing import LabelEncoder
from sklearn.preprocessing import OneHotEncoder
import json
from sklearn.metrics import accuracy_score, precision_recall_fscore_support, roc_auc_score
from sklearn.model_selection import train_test_split
from keras.callbacks import Callback
from keras.layers import Input, LSTM, RepeatVector, Flatten, Reshape, Dropout, Dense, TimeDistributed
from keras.models import Model
from keras.layers.normalization import BatchNormalization
from settings import DATA


def performTest(arch, benchmark, arch_num):
    fold = 0
    n_epochs = 40
    n_batch_size = 250
    dropout_rate = 0.5


    train_data_name = DATA[benchmark]["dir"] + str(benchmark) + "_kfoldcv_" + str(fold) + "_train.csv"
    test_data_name = DATA[benchmark]["dir"] + str(benchmark) + "_kfoldcv_" + str(fold) + "_test.csv"
    train_resource_name = DATA[benchmark]["dir"] + str(benchmark) + "_kfoldcv_" + str(fold) + "_train_resources.csv"
    test_resource_name = DATA[benchmark]["dir"] + str(benchmark) + "_kfoldcv_" + str(fold) + "_test_resources.csv"

    df_train = pd.read_csv(train_data_name, sep=';', header=None)
    df_test = pd.read_csv(test_data_name, sep=';', header=None)
    x = df_train.iloc[0]
    label_col = np.argwhere(x.isna())[0][0] - 1

    df_train_labels = df_train[label_col]
    df_test_labels = df_test[label_col]

    df_train = df_train.loc[:, :(label_col - 1)]
    df_test = df_test.loc[:, :(label_col - 1)]

    df_train_resources = pd.read_csv(train_resource_name, sep=';', header=None)
    df_test_resources = pd.read_csv(test_resource_name, sep=';', header=None)

    df_labels = np.unique(pd.concat([df_train_labels, df_test_labels]))

    X_train_res = np.asarray(df_train_resources)[:, 0:-1]
    X_test_res = np.asarray(df_test_resources)[:, 0:-1]

    label_encoder = LabelEncoder()
    integer_encoded = label_encoder.fit_transform(df_labels)
    integer_encoded = integer_encoded.reshape(len(integer_encoded), 1)
    onehot_encoder = OneHotEncoder(sparse=False)
    onehot_encoder.fit(integer_encoded)
    onehot_encoded = onehot_encoder.transform(integer_encoded)

    train_integer_encoded = label_encoder.transform(df_train_labels).reshape(-1, 1)
    train_onehot_encoded = onehot_encoder.transform(train_integer_encoded)
    X_train = np.asarray(df_train)
    Y_train = np.asarray(train_onehot_encoded)

    test_integer_encoded = label_encoder.transform(array(df_test_labels)).reshape(-1, 1)
    test_onehot_encoded = onehot_encoder.transform(test_integer_encoded)
    X_test = np.asarray(df_test)
    Y_test = np.asarray(test_onehot_encoded)
    Y_test_int = np.asarray(test_integer_encoded)

    stdScaler_res = MinMaxScaler()  # StandardScaler()
    stdScaler_res.fit(X_train_res)
    X_train_res = stdScaler_res.transform(X_train_res)
    X_test_res = stdScaler_res.transform(X_test_res)

    stdScaler = MinMaxScaler()  # StandardScaler()
    stdScaler.fit(X_train)
    X_train = stdScaler.transform(X_train)
    X_test = stdScaler.transform(X_test)

    X_train = np.concatenate([X_train, X_train_res], axis=1)
    X_test = np.concatenate([X_test, X_test_res], axis=1)

    X_train, X_val, Y_train, Y_val = train_test_split(X_train, Y_train, test_size=0.1, random_state=42, shuffle=True)

    insize = X_train.shape[1]
    outsize = len(onehot_encoded[0])
    res_insize = df_train_resources.shape[1]

    class_inputs = Input(shape=(insize,))

    layer = Dense(arch[0], activation='sigmoid')(class_inputs)
    layer = BatchNormalization()(layer)

    for i in range(len(arch)):
        if i != 0:
            layer = Dense(arch[i], activation='sigmoid', name="classifier_l" + str(i))(layer)
            layer = BatchNormalization()(layer)
            layer = Dropout(dropout_rate)(layer)

    out = Dense(outsize, activation='softmax', name="classifier")(layer)

    model = Model([class_inputs], [out])
    losses = {
        "classifier": "categorical_crossentropy",
    }

    model.compile(optimizer='adam', loss=losses, metrics=['accuracy'])
    model.summary()

    hist = model.fit([X_train], [Y_train], batch_size=n_batch_size, epochs=n_epochs, shuffle=True,
                        validation_data=([X_val], [Y_val]),
                        callbacks=[TestCallbackNew(X_test, Y_test, Y_test_int)])

    with open(str("results/" + benchmark + "/" + benchmark + "_" + str(arch_num) + "_results_napr-sigm-archsearch_" + str(h_layers[0]) + ".json"), 'w') as outfile:
            json.dump(hist.history, outfile)


def multiclass_roc_auc_score(y_test, y_pred, average="weighted"):
    lb = LabelBinarizer()
    lb.fit(y_test)
    y_test = lb.transform(y_test)
    y_pred = lb.transform(y_pred)
    return roc_auc_score(y_test, y_pred, average=average)

class TestCallbackNew(Callback):
    def __init__(self, X_test, Y_test, Y_test_int):
        self.X_test = X_test
        self.Y_test = Y_test
        self.Y_test_int = Y_test_int

        self.test_accs = []
        self.losses = []

    def on_train_begin(self, logs={}):
        self.test_accs = []
        self.losses = []

    def on_epoch_end(self, epoch, logs={}):
        y_pred = self.model.predict(self.X_test)
        y_pred = y_pred.argmax(axis=1)

        test_acc = accuracy_score(self.Y_test_int, y_pred, normalize=True)
        test_loss, _ = self.model.evaluate(self.X_test, self.Y_test)

        precision, recall, fscore, _ = precision_recall_fscore_support(self.Y_test_int, y_pred, average='weighted', pos_label=None)
        auc = multiclass_roc_auc_score(self.Y_test_int, y_pred, average="weighted")

        logs['test_acc'] = test_acc
        logs['test_prec_weighted'] = precision
        logs['test_rec_weighted'] = recall
        logs['test_loss'] = test_loss
        logs['test_fscore_weighted'] = fscore
        logs['test_auc_weighted'] = auc

        precision, recall, fscore, support = precision_recall_fscore_support(self.Y_test_int, y_pred, average='macro', pos_label=None)
        auc = multiclass_roc_auc_score(self.Y_test_int, y_pred, average="macro")
        logs['test_prec_mean'] = precision
        logs['test_rec_mean'] = recall
        logs['test_fscore_mean'] = fscore
        logs['test_auc_mean'] = auc

def isDecreasing(arch):
    decreases = True
    current = 1000000
    for i in arch:
        if i <= current:
            current = i
        else:
            decreases = False
    return decreases


if __name__ == "__main__":
    benchmark = "bpic13_p"

    neurons = [50, 100, 200, 300, 400, 500]
    h_layers = [5]
    arches = []

    """
    for layers in h_layers:
        for p in itertools.product(neurons, repeat=layers):
            if isDecreasing(p):
                arches.append(p)

    print(len(arches))
    """

    if h_layers[0] == 5:
        arches.append((300, 200, 200, 100, 50))
        arches.append((400, 300, 200, 100, 50))
        arches.append((500, 400, 300, 200, 100))
        arches.append((200, 50, 50, 50, 50) )
        arches.append((500, 500, 500, 500, 500))

    if h_layers[0] == 4:
        arches.append((300, 200, 100, 50))
        arches.append((400, 200, 100, 50))
        arches.append((400, 300, 200, 50))
        arches.append((400, 400, 100, 50))
        arches.append((500, 300, 100, 50))

    if h_layers[0] == 3:
        arches.append((200, 100, 50))
        arches.append((300, 200, 100))
        arches.append((400, 300, 100))
        arches.append((500, 300, 50))
        arches.append((500, 400, 300))

    outF = open("results/" + benchmark + "/" + benchmark + "_architectures_to_" + str(h_layers[0]) + "_napr-sigm.csv", "w")
    for line in arches:
        outF.write(str(line))
        outF.write("\n")
    outF.close()


    for i in range(0, len(arches)):
        print("Perform on architecture number ", i)
        performTest(arches[i], benchmark, i)






