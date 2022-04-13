from sklearn.metrics import mean_squared_error, r2_score
from math import sqrt


def rmse(y_true, y_pred):
	return sqrt(mean_squared_error(y_true, y_pred))


def r2(y_true, y_pred):
    return r2_score(y_true, y_pred)
