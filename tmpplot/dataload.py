from plumbum import local

import re

class ExperimentsData(object):
    def __init__(self, filename):
        self._data = list(stream_data(filename))

        self._data_point_keys = set()
        self._experiment_result_keys = set()
        self._by_seed = {}

        for data_point, seed, experiment_result in self._data:
            self._data_point_keys |= data_point._fields.keys()
            if isinstance(experiment_result, ExperimentResult):
                self._experiment_result_keys |= experiment_result._fields.keys()
                self._by_seed.setdefault(seed, ResultsForSeed())._data.append((data_point, experiment_result))

        self._by_seed = {s: bs for s, bs in self._by_seed.items() if bs.single_robot_results}

        self._by_data_point = {}
        for rfs in self.per_seed:
            single_robot = rfs.single_robot_results
            for dp, er in rfs._data:
                self._by_data_point.setdefault(dp, []).append({
                    k: (getattr(er, k) / getattr(single_robot, k)
                        if k != 'coverageRedundancy'
                        else getattr(er, k))
                    for k in self._experiment_result_keys
                })



    def __str__(self):
        return '%s records of (%s) -> (%s)' % (len(self._data), ', '.join(self._data_point_keys), ', '.join(self._experiment_result_keys))


    @property
    def per_seed(self):
        return self._by_seed.values()

    def get_pairs(self, xfield, agg, yfield, **filters):
        for dp, ers in self._by_data_point.items():
            if any(getattr(dp, k) != v for k, v in filters.items()):
                continue
            yield getattr(dp, xfield), agg(er[yfield] for er in ers)

    def plot(self, xfield, agg, yfield, **filters):
        import matplotlib.pyplot as plt

        xs = []
        ys = []
        for x, y in sorted(self.get_pairs(xfield, agg, yfield, **filters)):
            xs.append(x)
            ys.append(y)

        assert len(xs) == len(set(xs))

        plt.xlabel(xfield)
        plt.ylabel('%s of %s' % (agg.__name__, yfield))
        plt.plot(xs, ys)
        plt.show()


class ResultsForSeed(object):
    def __init__(self):
        self._data = []

    @property
    def single_robot_results(self):
        for dp, er in self._data:
            if 1 == dp.numberOfRobots:
                return er

    @property
    def two_robots(self):
        for dp, er in self._data:
            if 2 == dp.numberOfRobots:
                yield dp, er

    def __repr__(self):
        return '[%s]' % ', '.join('%s -> %s' % kv for kv in self._data)


def stream_data(filename):
    with local.path(filename).open() as f:
        line = f.readline()
        while line:
            yield parse_line(line)
            line = f.readline()


def parse_line(line):
    data_point, seed, experiment_result = line.strip().split('\t')
    data_point = DataPoint.create(data_point)
    seed = int(seed)
    try:
        experiment_result = ExperimentResult.create(experiment_result)
    except ValueError:
        if experiment_result == 'null':
            experiment_result = None
    return data_point, seed, experiment_result


class JavaObj(object):
    _parse_pattern = re.compile(r'^(\w+)\((.*)\)$')

    @classmethod
    def create(cls, source):
        fields = {}
        m = cls._parse_pattern.match(source)
        if m is None:
            raise ValueError
        typename, data = m.groups()
        assert typename == cls.__name__
        for field in data.split(', '):
            k, v = field.split('=', 2)
            for t in [int, float]:
                try:
                    v = t(v)
                except ValueError:
                    pass
                else:
                    break
            fields[k] = v

        return cls(**fields)

    def __init__(self, **fields):
        self._fields = fields
        for k, v in fields.items():
            setattr(self, k, v)
        self._hash = hash((int,) + tuple(sorted(fields.items())))

    def __repr__(self):
        return '%s(%s)' % (type(self).__name__,
                           ', '.join('%s=%s' % pair
                                     for pair in self._fields.items()))

    def __hash__(self):
        return self._hash

    def __eq__(self, other):
        return type(self) == type(other) and self._fields == other._fields

    def __ne__(self, other):
        return not(self == other)


class DataPoint(JavaObj):
    pass


class ExperimentResult(JavaObj):
    pass
