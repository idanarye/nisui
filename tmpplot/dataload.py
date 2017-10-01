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

    def get_pairs(self, dp_field, agg, rs_field, *predicates, **filters):
        for dp, ers in self._by_data_point.items():
            if not self._check_filters(dp, *predicates, **filters):
                continue
            # if any(getattr(dp, k) != v for k, v in filters.items()):
                # continue
            # if not all(predicate(dp) for predicate in predicates):
                # continue
            yield tuple(f(dp) for f in dp_field), agg(rs_field(er) for er in ers)

    def get_success_histogram(self):
        result = {}
        for dp, _, er in self._data:
            try:
                dp_dict = result[dp]
            except KeyError:
                dp_dict = result[dp] = {True: 0, False: 0}
            dp_dict[er is not None] += 1
        return result

    def plot(self, dp_fields, agg, rs_field, *predicates, group_by='', filename=None, **filters):
        import matplotlib.pyplot as plt
        plt.figure(figsize=(10, 10))

        if not isinstance(dp_fields, tuple):
            dp_fields = (dp_fields,)
        dp_labels, dp_fields = _field_getter(dp_fields)
        rs_label, rs_field = _field_getter(rs_field)

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

            all_filters = [(group, make_filter(group)) for group in all_groups]
        else:
            def make_label(group):
                assert group is None
                return None
            all_filters = [(None, filters)]

        all_xs = set()
        for group_by_tuple, filters in sorted(all_filters):
            label = make_label(group_by_tuple)
            dp_values = tuple([] for _ in dp_fields)
            res_values = []
            for dp, y in sorted(self.get_pairs(dp_fields, agg, rs_field, *predicates, **filters)):
                for vs, p in zip(dp_values, dp):
                    vs.append(p)
                res_values.append(y)

            assert sum(1 for _ in zip(*dp_values)) == len(set(zip(*dp_values)))

            if len(dp_values) == 1:
                plt.plot(*dp_values, res_values, marker='.', label=label)
                all_xs.update(*dp_values)
            elif len(dp_values) == 2:
                from mpl_toolkits.mplot3d import Axes3D
                from matplotlib import cm
                from matplotlib.ticker import LinearLocator, FormatStrFormatter

                fig = plt.figure()
                ax = fig.gca(projection='3d')
                ax.set_xlabel(dp_labels[0])
                ax.set_ylabel(dp_labels[1])
                ax.set_zlabel('%s of %s' % (agg.__name__, rs_label))
                surf = ax.plot_trisurf(*dp_values, res_values, cmap=cm.coolwarm, linewidth=1, antialiased=True, shade=True)
                if group_by:
                    fig.suptitle(label)

        if len(dp_fields) == 1:
            baseline = agg(rs_field(rfs.single_robot_results) for rfs in self.per_seed if rfs.single_robot_results)
            plt.plot(sorted(all_xs), [baseline] * len(all_xs), 'k--')

            plt.xlabel(*dp_labels)
            plt.ylabel('%s of %s' % (agg.__name__, rs_label))
            if group_by:
                plt.legend(ncol=3, loc=9)

        plt.tight_layout()
        if filename:
            plt.savefig(filename)
        else:
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
            else:
                if v == 'true':
                    v = True
                elif v == 'false':
                    v = False
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
    if isinstance(field, tuple):
        return zip(*map(_field_getter, field))
    if isinstance(field, str):
        def getter(obj):
            return obj[field]
        return field, getter
    return field.__name__, field
