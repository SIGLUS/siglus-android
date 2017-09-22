module AssertModule
  class AssertionError < RuntimeError
    def initialize
      super('Expected condition to be true, but it wasn\'t')
    end
  end

  def assert(&block)
    raise AssertionError unless yield
  end
end
