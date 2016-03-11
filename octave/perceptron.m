% ----------------------------- LOAD DATA ----------------------------- %

% Load training features
trFe = spconvert(load('data/rcv1.train.features'));

% Load training labels
trLa = load('data/rcv1.train.labels');

% Save training number of lines (features) and number of attributes
trNumL = size(trLa)(1);
trNumA = size(trLa)(2);

% Load testing features
teFe = spconvert(load('data/rcv1.test.features'));

% Load testing labels
teLa = load('data/rcv1.test.labels');

% Save testing number of lines (features) and number of attributes
teNumL = size(teLa)(1);
teNumA = size(teLa)(2);

% ----------------------------- FUNCTIONS ----------------------------- %

% Perceptron error identifier
function res1 = mistake(X, Y, w, b, i)
    res1 = Y(i) * (dot(w, X(i, :)) + b);
end

% Error calculator function
function res2 = errors(numL, fe, la, w, b)
    err = 0;
    
    for i = 1 : numL
        if mistake(fe, la, w, b, i) <= 0
            err = err + 1;
        end
    end

    res2 = err/numL;
end

% ---------------------------- CALCULATE R ---------------------------- %

R = 0;

for i = 1 : trNumL
    nx = norm( trLa(i, :) );
    
    if nx > R
        R = nx;
    end
end

% --------------------------- TRAINING PHASE -------------------------- %

orders = load('data/training.orders');
rows = size(orders)(1);
cols = size(orders)(2);

for i = 1 : rows
    disp(i)
    % Initialize weight vector and bias vector to zero
    w = zeros(1, trNumA);
    b = zeros(1, trNumA);

    % Start mistakes at 1 and set a value for learning n
    k = 1;
    n = 1;

    for j = 1 : cols
        index = orders(i, j);

        if mistake(trFe, trLa, w, b, index) <= 0
            w = w + n * trLa(index) * trFe(index, :);
            b = b + n * trLa(index) * R ^ 2;
            k = k + 1;
        end

        if(mod(j, 100) == 0)
            trErr = errors(trNumL, trFe, trLa, w, b);
            teErr = errors(teNumL, teFe, teLa, w, b);

            disp([trErr, teErr])
        end
    end
end

%{

for i=1:l
    if test(X,Y,w,b,i) <= 0
        %w(k+1,:) = w(k,:)+n*Y(i)*X(i,:)
        %b(k+1,:) = b(k,:)+n*Y(i)*R^2
        %w = [w; w(k,:)+n*Y(i)*X(i,:)];
        %b = [b; b(k,:)+n*Y(i)*R^2];
        w = w+n*Y(i)*X(i,:);
        b = b+n*Y(i)*R^2;
        disp([i,k])
        k=k+1;
    end
end

disp("Loading test data...")

TX = load("data/rcv1.test.features");
X = spconvert(TX);
Y = load("data/rcv1.test.labels"); %spconvert(TY)
l = size(X)(1);

disp("Testing on training data...")

errTrain = 0;
totalTrain = 0;
for i=1:l
    if test(X,Y,w,b,i) <= 0
        errTrain=errTrain+1;
    end
    disp([i,errTrain,totalTrain])
    totalTrain=totalTrain+1;
end

errTrain/totalTrain
%}