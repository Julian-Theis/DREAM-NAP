from numpy.random import seed
seed(1)
from tensorflow import set_random_seed
set_random_seed(2)
from numpy.random import seed
seed(1)
from tensorflow import set_random_seed
set_random_seed(2)

import numpy as np
np.seterr(divide='ignore', invalid='ignore')

import pandas as pd
from sklearn.preprocessing import MinMaxScaler, StandardScaler, LabelBinarizer
from numpy import array
from sklearn.preprocessing import LabelEncoder
from sklearn.preprocessing import OneHotEncoder
import json
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, precision_recall_fscore_support, roc_auc_score
from keras.callbacks import Callback
from keras.layers import Dropout, Dense
from keras.models import Sequential
from settings import DATA


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
        y_pred = model.predict(self.X_test)
        y_pred = y_pred.argmax(axis=1)

        test_acc = accuracy_score(self.Y_test_int, y_pred, normalize=True)
        test_loss, _ = model.evaluate(self.X_test, self.Y_test)

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


if __name__ == "__main__":
    n_folds = 10
    n_epochs = 100
    n_batch_size = 64
    dropout_rate = 0.2

    benchmark = "bpic12_o"

    x_train_folds = []
    x_test_folds = []
    y_train_folds = []
    y_test_folds = []
    y_test_int_folds = []

    for fold in range(n_folds):
        folder = ""

        if fold == 0:
            n_epochs = 100
        else:
            n_epochs = 40

        n_batch_size = 64  # 20
        dropout_rate = 0.2

        train_data_name = DATA[benchmark]["dir"] + str(benchmark) + "_kfoldcv_" + str(fold) + "_train.csv"
        test_data_name = DATA[benchmark]["dir"] + str(benchmark) + "_kfoldcv_" + str(fold) + "_test.csv"

        df_train = pd.read_csv(train_data_name, sep=';', header=None)
        df_test = pd.read_csv(test_data_name, sep=';', header=None)
        x = df_train.iloc[0]

        if benchmark != "helpdesk":
            label_col = np.argwhere(x.isna())[0][0] - 1
        else:
            label_col = df_train.shape[1] - 1

        df_train_labels = df_train[label_col]
        df_test_labels = df_test[label_col]

        df_train = df_train.loc[:, :(label_col - 1)]
        df_test = df_test.loc[:, :(label_col - 1)]

        df_labels = np.unique(pd.concat([df_train_labels, df_test_labels]))

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

        stdScaler = MinMaxScaler()  # StandardScaler()
        stdScaler.fit(X_train)
        X_train = stdScaler.transform(X_train)
        X_test = stdScaler.transform(X_test)

        X_train, X_val, Y_train, Y_val = train_test_split(X_train, Y_train, test_size=0.1, random_state=42,
                                                          shuffle=True)

        insize = X_train.shape[1]
        outsize = len(onehot_encoded[0])
        print("In:", insize, "Out: ", outsize)

        # CREATE MODEL
        model = Sequential()
        model.add(Dense(insize, input_dim=insize, activation='relu'))
        model.add(Dropout(dropout_rate))
        model.add(Dense(int(insize * 1.2), activation='relu'))
        model.add(Dropout(dropout_rate))
        model.add(Dense(int(insize * 0.6), activation='relu'))
        model.add(Dropout(dropout_rate))
        model.add(Dense(int(insize * 0.3), activation='relu'))
        model.add(Dropout(dropout_rate))
        model.add(Dense(outsize, activation='softmax'))
        # Compile model
        model.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['accuracy'])
        # Fit the model
        hist = model.fit([X_train], [Y_train], batch_size=n_batch_size, epochs=n_epochs, shuffle=True,
                         validation_data=([X_val], [Y_val]),
                         callbacks=[TestCallbackNew(X_test, Y_test, Y_test_int)])

        with open(str("results/" + benchmark + "/" + benchmark + "_" + str(fold) + "_results_nap.json"), 'w') as outfile:
            json.dump(hist.history, outfile)











