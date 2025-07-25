package net.microfalx.metrics;

import net.microfalx.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringJoiner;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.rethrowExceptionAndReturn;

/**
 * A result of a {@link Query}.
 */
public final class Result implements Cloneable {

    private final Query query;
    private final Type type;
    private final boolean successful;
    private String message;

    final Collection<Matrix> matrixes = new ArrayList<>();
    final Collection<Vector> vectors = new ArrayList<>();
    private Value value;
    private String text;

    static Result failed(Query query) {
        return new Result(query, Type.SCALAR, false);
    }

    static Result success(Query query, Type type) {
        return new Result(query, type, true);
    }

    public static Result matrix(Query query, Collection<Matrix> matrixes) {
        Result result = new Result(query, Type.MATRIX, true);
        result.matrixes.addAll(matrixes);
        return result;
    }

    public static Result vector(Query query, Collection<Vector> vectors) {
        Result result = new Result(query, Type.VECTOR, true);
        result.vectors.addAll(vectors);
        return result;
    }

    public static Result scalar(Query query, Value value) {
        Result result = new Result(query, Type.SCALAR, true);
        result.value = value;
        return result;
    }

    static Result text(Query query, String text) {
        Result result = new Result(query, Type.STRING, true);
        result.text = text;
        return result;
    }

    private Result(Query query, Type type, boolean successful) {
        requireNonNull(query);
        requireNonNull(type);
        this.query = query;
        this.type = type;
        this.successful = successful;
    }

    public Query getQuery() {
        return query;
    }

    public Type getType() {
        return type;
    }

    public Collection<Matrix> getMatrixes() {
        return unmodifiableCollection(matrixes);
    }

    public Collection<Vector> getVectors() {
        return unmodifiableCollection(vectors);
    }

    public Value getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    public boolean isEmpty() {
        switch (type) {
            case STRING:
                return StringUtils.isEmpty(text);
            case SCALAR:
                return value == null;
            case VECTOR:
                return vectors.isEmpty();
            case MATRIX:
                return matrixes.isEmpty();
            default:
                return true;
        }
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getMessage() {
        return message;
    }

    public Result withMatrixes(Collection<Matrix> matrixes) {
        Result copy = copy();
        copy.matrixes.clear();
        copy.matrixes.addAll(matrixes);
        return copy;
    }

    public Result withVectors(Collection<Vector> vectors) {
        Result copy = copy();
        copy.vectors.clear();
        copy.vectors.addAll(vectors);
        return copy;
    }

    public Result withValue(Value value) {
        Result copy = copy();
        copy.value = value;
        return copy;
    }

    public Result withText(String text) {
        Result copy = copy();
        copy.text = text;
        return copy;
    }

    public Result withMessage(String message) {
        Result copy = copy();
        copy.message = message;
        return copy;
    }

    private Result copy() {
        try {
            return (Result) clone();
        } catch (CloneNotSupportedException e) {
            return rethrowExceptionAndReturn(e);
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Result.class.getSimpleName() + "[", "]")
                .add("query=" + query)
                .add("type=" + type)
                .add("successful=" + successful)
                .add("message='" + message + "'")
                .add("matrixes=" + matrixes.size())
                .add("vectors=" + vectors.size())
                .add("value=" + value)
                .add("text='" + text + "'")
                .toString();
    }

    public enum Type {
        MATRIX,
        VECTOR,
        SCALAR,
        STRING
    }
}
