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

        # self._by_seed = {s: bs for s, bs in self._by_seed.items() if bs.single_robot_results}
        self._by_seed = {s: bs for s, bs in self._by_seed.items()}  # if bs.single_robot_results}

        self._by_data_point = {}
        for rfs in self.per_seed:
            single_robot = rfs.single_robot_results
            for dp, er in rfs._data:
                self._by_data_point.setdefault(dp, []).append({
                    # k: (getattr(er, k) / getattr(single_robot, k)
                    k: (getattr(er, k)
                        if k != 'coverageRedundancy'
                        else getattr(er, k))
                    for k in self._experiment_result_keys
                })



    def __str__(self):
        return '%s records of (%s) -> (%s)' % (len(self._data), ', '.join(self._data_point_keys), ', '.join(self._experiment_result_keys))


    @property
    def per_seed(self):
        return self._by_seed.values()

    @staticmethod
    def _check_filters(dp, *predicates, **filters):
        if any(getattr(dp, k) != v for k, v in filters.items()):
            return False
        if not all(predicate(dp) for predicate in predicates):
            return False
        return True

    def get_pairs(self, xfield, agg, yfield, *predicates, **filters):
        for dp, ers in self._by_data_point.items():
            if not self._check_filters(dp, *predicates, **filters):
                continue
            # if any(getattr(dp, k) != v for k, v in filters.items()):
                # continue
            # if not all(predicate(dp) for predicate in predicates):
                # continue
            yield xfield(dp), agg(yfield(er) for er in ers)

    def plot(self, xfield, agg, yfield, *predicates, group_by='', **filters):
        import matplotlib.pyplot as plt

        xlabel, xfield = _field_getter(xfield)
        ylabel, yfield = _field_getter(yfield)

        if group_by:
            if isinstance(group_by, str):
                group_by = [group_by]
            else:
                group_by == list(group_by)

            def group_by_tuple(dp):
                return tuple(getattr(dp, k) for k in group_by)

            all_groups = set(group_by_tuple(dp)
                             for dp in self._by_data_point.keys()
                             if self._check_filters(dp, *predicates, **filters))

            def make_label(group):
                return ', '.join('%s=%s' % pair for pair in zip(group_by, group))

            def make_filter(group):
                return dict(zip(group_by, group), **filters)

            all_filters = [(make_label(group), make_filter(group)) for group in all_groups]
        else:
            all_filters = [(None, filters)]

        all_xs = set()
        for label, filters in all_filters:
            xs = []
            ys = []
            for x, y in sorted(self.get_pairs(xfield, agg, yfield, *predicates, **filters)):
                xs.append(x)
                ys.append(y)

            assert len(xs) == len(set(xs))

            plt.plot(xs, ys, marker='.', label=label)
            all_xs.update(xs)

        baseline = agg(yfield(rfs.single_robot_results) for rfs in self.per_seed if rfs.single_robot_results)
        plt.plot(sorted(all_xs), [baseline] * len(all_xs), 'k--')

        plt.xlabel(xlabel)
        plt.ylabel('%s of %s' % (agg.__name__, ylabel))
        if group_by:
            plt.legend(ncol=3, loc=9)

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

    def __getitem__(self, name):
        return getattr(self, name)


class DataPoint(JavaObj):
    pass


class ExperimentResult(JavaObj):
    pass


def _field_getter(field):
    if isinstance(field, str):
        def getter(obj):
            return obj[field]
        return field, getter
    return field.__name__, field
